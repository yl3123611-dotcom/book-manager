package com.book.manager.modules.report;

import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.UsersMapper;
import com.book.manager.entity.Users;
import com.book.manager.service.ReadingReportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class ReadingReportScheduler {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private ReadingReportService readingReportService;

    /**
     * 每天凌晨 04:10 生成/更新当周阅读报告
     */
    @Scheduled(cron = "0 10 4 * * ?")
    public void buildWeekly() {
        try {
            List<Users> users = usersMapper.findAll();
            if (users == null || users.isEmpty()) return;
            java.util.Date now = new java.util.Date();
            Date ps = new Date(DateUtil.beginOfDay(DateUtil.beginOfWeek(now)).getTime());
            Date pe = new Date(DateUtil.beginOfDay(DateUtil.endOfWeek(now)).getTime());
            for (Users u : users) {
                if (u == null || u.getId() == null) continue;
                readingReportService.buildAndSave(u.getId(), ps, pe);
            }
        } catch (Exception e) {
            log.error("ReadingReportScheduler buildWeekly error", e);
        }
    }
}
