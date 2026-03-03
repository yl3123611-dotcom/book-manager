package com.book.manager.service;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BorrowMapper;
import com.book.manager.dao.ReadingReportMapper;
import com.book.manager.dao.UserActionMapper;
import com.book.manager.entity.Book;
import com.book.manager.entity.Borrow;
import com.book.manager.entity.ReadingReport;
import com.book.manager.entity.UserAction;
import com.book.manager.util.consts.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

@Service
public class ReadingReportService {

    @Autowired
    private ReadingReportMapper readingReportMapper;

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private BookService bookService;

    public ReadingReport getOrBuild(Integer userId, String period) {
        if (userId == null) return null;
        if (period == null || period.isBlank()) period = "week";

        java.util.Date now = new java.util.Date();
        java.util.Date start;
        java.util.Date end;
        if ("month".equalsIgnoreCase(period)) {
            start = DateUtil.beginOfMonth(now);
            end = DateUtil.endOfMonth(now);
        } else {
            // week: Monday ~ Sunday
            start = DateUtil.beginOfWeek(now);
            end = DateUtil.endOfWeek(now);
        }
        Date ps = new Date(DateUtil.beginOfDay(start).getTime());
        Date pe = new Date(DateUtil.beginOfDay(end).getTime());

        ReadingReport existing = readingReportMapper.findByUserAndPeriod(userId, ps, pe);
        if (existing != null) return existing;
        return buildAndSave(userId, ps, pe);
    }

    @Transactional
    public ReadingReport buildAndSave(Integer userId, Date periodStart, Date periodEnd) {
        // 统计借阅记录（以 create_time 落在周期内作为一次阅读）
        List<Borrow> borrows = borrowMapper.findAllBorrowByUserId(userId);
        Set<Integer> distinctBookIds = new HashSet<>();
        int pagesRead = 0;
        int booksRead = 0;
        if (borrows != null) {
            for (Borrow b : borrows) {
                if (b == null) continue;
                if (b.getBookId() == null) continue;
                if (b.getCreateTime() == null) continue;

                java.util.Date ct = b.getCreateTime();
                if (ct.before(periodStart) || ct.after(DateUtil.endOfDay(periodEnd))) continue;

                // 未归还也算阅读了一本
                distinctBookIds.add(b.getBookId());
            }
        }

        for (Integer bid : distinctBookIds) {
            Book book = bookService.findBook(bid);
            if (book != null && book.getPages() != null) {
                pagesRead += Math.max(0, book.getPages());
            }
        }
        booksRead = distinctBookIds.size();

        // 平均阅读时长：用 VIEW 行为粗略估算（每次 view 记 6 分钟，可调整）
        int viewCount = 0;
        List<UserAction> actions = userActionMapper.listByUser(userId, 1000);
        if (actions != null) {
            for (UserAction a : actions) {
                if (a == null) continue;
                if (!"VIEW".equalsIgnoreCase(a.getActionType())) continue;
                if (a.getCreatedAt() == null) continue;
                if (a.getCreatedAt().before(periodStart) || a.getCreatedAt().after(DateUtil.endOfDay(periodEnd))) continue;
                viewCount++;
            }
        }
        int totalSessionMinutes = viewCount * 6;
        int avgSessionMinutes = viewCount == 0 ? 0 : Math.max(1, totalSessionMinutes / viewCount);

        ReadingReport r = new ReadingReport();
        r.setUserId(userId);
        r.setBooksRead(booksRead);
        r.setPagesRead(pagesRead);
        r.setAvgSessionMinutes(avgSessionMinutes);
        r.setPeriodStart(periodStart);
        r.setPeriodEnd(periodEnd);

        ReadingReport existing = readingReportMapper.findByUserAndPeriod(userId, periodStart, periodEnd);
        if (existing == null) {
            readingReportMapper.insert(r);
        } else {
            r.setId(existing.getId());
            readingReportMapper.update(r);
        }
        return readingReportMapper.findByUserAndPeriod(userId, periodStart, periodEnd);
    }
}
