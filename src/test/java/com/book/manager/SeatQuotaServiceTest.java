package com.book.manager;

import cn.hutool.core.date.DateUtil;
import com.book.manager.entity.SeatReservationSlotQuota;
import com.book.manager.service.SeatQuotaService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@SpringBootTest
class SeatQuotaServiceTest {

    @Autowired
    private SeatQuotaService seatQuotaService;

    @Test
    @Transactional
    void save_shouldInsertThenUpdateById() {
        Date day = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), 1));
        Date start = DateUtil.parse(DateUtil.formatDate(day) + " 09:00:00", "yyyy-MM-dd HH:mm:ss");
        Date end = DateUtil.parse(DateUtil.formatDate(day) + " 11:00:00", "yyyy-MM-dd HH:mm:ss");

        SeatReservationSlotQuota create = new SeatReservationSlotQuota();
        create.setSlotDate(day);
        create.setSlotStart(start);
        create.setSlotEnd(end);
        create.setRoom("测试更新阅览室");
        create.setMaxCount(5);

        Assertions.assertTrue(seatQuotaService.save(create));

        SeatReservationSlotQuota stored = seatQuotaService.find(day, start, end, "测试更新阅览室");
        Assertions.assertNotNull(stored);
        Assertions.assertNotNull(stored.getId());
        Assertions.assertEquals(5, stored.getMaxCount());

        Date newStart = DateUtil.parse(DateUtil.formatDate(day) + " 10:00:00", "yyyy-MM-dd HH:mm:ss");
        Date newEnd = DateUtil.parse(DateUtil.formatDate(day) + " 12:00:00", "yyyy-MM-dd HH:mm:ss");

        SeatReservationSlotQuota update = new SeatReservationSlotQuota();
        update.setId(stored.getId());
        update.setSlotDate(day);
        update.setSlotStart(newStart);
        update.setSlotEnd(newEnd);
        update.setRoom("测试更新阅览室");
        update.setMaxCount(8);

        Assertions.assertTrue(seatQuotaService.save(update));

        SeatReservationSlotQuota updated = seatQuotaService.find(day, newStart, newEnd, "测试更新阅览室");
        Assertions.assertNotNull(updated);
        Assertions.assertEquals(stored.getId(), updated.getId());
        Assertions.assertEquals(8, updated.getMaxCount());
    }
}

