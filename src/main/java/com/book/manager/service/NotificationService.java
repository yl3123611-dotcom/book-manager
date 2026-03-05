package com.book.manager.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.NotificationMapper;
import com.book.manager.entity.Book;
import com.book.manager.entity.Borrow;
import com.book.manager.entity.Notification;
import com.book.manager.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    /** 查询用户通知（可按状态筛选） */
    public List<Notification> list(Integer userId, Integer status) {
        List<Notification> list = notificationMapper.selectByUser(userId, status);
        enrichBorrowBookName(list);
        return list;
    }

    public List<Notification> archivedList(Integer userId) {
        List<Notification> list = notificationMapper.selectArchivedByUser(userId);
        enrichBorrowBookName(list);
        return list;
    }

    public Notification detail(Integer userId, Integer id) {
        Notification n = notificationMapper.selectById(id, userId);
        if (n == null) {
            return null;
        }
        enrichBorrowBookName(List.of(n));
        return n;
    }

    public List<Map<String, Object>> readReceipt(Integer userId, Integer id) {
        Notification current = notificationMapper.selectById(id, userId);
        if (current == null) {
            return List.of();
        }
        return notificationMapper.readReceiptByNotificationId(id);
    }

    public int unreadCount(Integer userId) {
        return notificationMapper.countUnread(userId);
    }

    public boolean markRead(Integer userId, Integer id) {
        return notificationMapper.markRead(id, userId) > 0;
    }

    public int markAllRead(Integer userId) {
        return notificationMapper.markAllRead(userId);
    }

    public boolean sign(Integer userId, Integer id) {
        return notificationMapper.sign(id, userId) > 0;
    }

    public boolean reply(Integer userId, Integer id, String replyContent) {
        if (replyContent == null || replyContent.trim().isEmpty()) {
            return false;
        }
        return notificationMapper.reply(id, userId, replyContent.trim()) > 0;
    }

    public boolean archive(Integer userId, Integer id) {
        return notificationMapper.archive(id, userId) > 0;
    }

    public boolean unarchive(Integer userId, Integer id) {
        return notificationMapper.unarchive(id, userId) > 0;
    }

    public boolean remove(Integer userId, Integer id) {
        return notificationMapper.remove(id, userId) > 0;
    }

    private void enrichBorrowBookName(List<Notification> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        for (Notification n : list) {
            if (n == null || n.getBorrowId() == null) continue;
            if (("DUE_REMINDER".equals(n.getType()) || "OVERDUE_ALERT".equals(n.getType()))
                    && n.getContent() != null && !n.getContent().contains("《")) {
                Borrow borrow = borrowService.findById(n.getBorrowId());
                if (borrow == null || borrow.getBookId() == null) continue;
                Book book = bookService.findBook(borrow.getBookId());
                if (book == null || book.getName() == null || book.getName().isBlank()) continue;

                String bookName = "《" + book.getName().trim() + "》";
                String content = n.getContent();
                content = content.replaceAll("\\(id=\\d+\\)", bookName);
                if (!content.contains(bookName)) {
                    content = "您借阅的" + bookName + "，" + content;
                }
                n.setContent(content);
            }
        }
    }

    /**
     * 生成到期/逾期提醒：
     * - 未归还 ret=NO
     * - endTime <= now + dueDays: DUE_REMINDER
     * - endTime < now: OVERDUE_ALERT
     */
    public int generateBorrowNotifications(Integer dueDays) {
        if (dueDays == null || dueDays < 0) dueDays = 1;

        Date now = new Date();
        Date dueThreshold = DateUtil.offsetDay(now, dueDays);

        List<Borrow> active = borrowService.findAllActive();
        int created = 0;

        for (Borrow b : active) {
            if (b.getEndTime() == null) continue;
            // 简单去重：同一条借阅记录，距离上次提醒不足6小时则跳过
            if (b.getLastNotify() != null && DateUtil.between(b.getLastNotify(), now, DateUnit.HOUR, true) < 6) {
                continue;
            }

            String type;
            String title;
            String content;
            String bookName = "该书籍";
            if (b.getBookId() != null) {
                Book book = bookService.findBook(b.getBookId());
                if (book != null && book.getName() != null && !book.getName().isBlank()) {
                    bookName = "《" + book.getName().trim() + "》";
                }
            }

            if (now.after(b.getEndTime())) {
                type = "OVERDUE_ALERT";
                title = "图书逾期提醒";
                content = "您借阅的" + bookName + "已逾期，请尽快归还。";
            } else if (!b.getEndTime().after(dueThreshold)) {
                type = "DUE_REMINDER";
                title = "图书即将到期提醒";
                content = "您借阅的" + bookName + "将于 " + DateUtil.formatDateTime(b.getEndTime()) + " 到期，请及时归还或续借。";
            } else {
                continue;
            }

            Users u = userService.findUserById(b.getUserId());
            if (u == null) continue;

            Notification n = new Notification();
            n.setBorrowId(b.getId());
            n.setUserId(u.getId());
            n.setType(type);
            n.setTitle(title);
            n.setContent(content);
            n.setStatus(0);
            n.setAttempts(0);
            n.setSenderUserId(0);

            notificationMapper.insert(n);
            created++;

            // 更新 last_notify
            b.setLastNotify(now);
            borrowService.updateBorrow(b);
        }

        return created;
    }
}
