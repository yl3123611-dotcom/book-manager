package com.book.manager.modules.notify;

import com.book.manager.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
public class BorrowNotifyScheduler {

    @Autowired
    private NotificationService notificationService;

    /**
     * 每小时扫描一次：生成即将到期/逾期提醒
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void runHourly() {
        try {
            int created = notificationService.generateBorrowNotifications(1);
            if (created > 0) {
                log.info("Generated {} borrow notifications", created);
            }
        } catch (Exception e) {
            log.error("Borrow notify scheduler failed", e);
        }
    }
}
