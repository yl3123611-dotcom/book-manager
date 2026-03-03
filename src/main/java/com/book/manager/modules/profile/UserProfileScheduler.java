package com.book.manager.modules.profile;

import com.book.manager.dao.UsersMapper;
import com.book.manager.entity.Users;
import com.book.manager.service.UserProfileService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@EnableScheduling
public class UserProfileScheduler {

    @Autowired
    private UsersMapper usersMapper;

    @Autowired
    private UserProfileService userProfileService;

    /**
     * 每天凌晨 03:30 重新计算画像（避免影响白天性能）
     */
    @Scheduled(cron = "0 30 3 * * ?")
    public void recomputeAll() {
        try {
            List<Users> users = usersMapper.findAll();
            if (users == null || users.isEmpty()) return;
            for (Users u : users) {
                if (u == null || u.getId() == null) continue;
                userProfileService.computeAndSave(u.getId());
            }
        } catch (Exception e) {
            log.error("UserProfileScheduler recomputeAll error", e);
        }
    }
}
