package com.book.manager;

import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.SeatMapper;
import com.book.manager.entity.ReadingRoom;
import com.book.manager.entity.Seat;
import com.book.manager.entity.SeatReservationSlotQuota;
import com.book.manager.entity.Users;
import com.book.manager.service.SeatQuotaService;
import com.book.manager.service.SeatReservationService;
import com.book.manager.service.SeatRuleService;
import com.book.manager.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@SpringBootTest
public class SeatReservationServiceTest {

    @Autowired
    SeatReservationService seatReservationService;

    @Autowired
    SeatMapper seatMapper;

    @Autowired
    SeatQuotaService seatQuotaService;

    @Autowired
    SeatRuleService seatRuleService;

    @Autowired
    UserService userService;

    @Test
    @Transactional
    void reserveSlot_requiresQuota() {
        // create a user
        Users u = new Users();
        u.setUsername("slot_test_user");
        u.setPassword("123456");
        userService.addUser(u);

        // create room/seat
        ReadingRoom room = new ReadingRoom();
        room.setName("测试阅览室");
        room.setEnabled(1);
        room.setLocation("T");
        room.setSize(0);
        seatMapper.insertRoom(room);

        Seat seat = new Seat();
        seat.setSeatNo("T-001");
        seat.setRoom(room.getName());
        seat.setStatus(0);
        seatMapper.insertSeat(seat);

        // ✅ 使用相对时间，避免固定日期导致“超出可预约天数/过去时间”等规则失败
        Date now = new Date();
        Date start = DateUtil.offsetHour(now, 1);
        Date end = DateUtil.offsetHour(now, 3);

        // no quota configured -> should fail
        String err = seatReservationService.reserveSlot(u.getUsername(), seat.getId(), start, end);
        Assertions.assertNotNull(err);
        Assertions.assertTrue(err.contains("未配置配额"));

        // add quota
        SeatReservationSlotQuota q = new SeatReservationSlotQuota();
        q.setSlotDate(DateUtil.beginOfDay(start));
        q.setSlotStart(start);
        q.setSlotEnd(end);
        q.setRoom(room.getName());
        q.setReservedCount(0);
        q.setMaxCount(1);
        Assertions.assertTrue(seatQuotaService.save(q));

        // now should succeed
        err = seatReservationService.reserveSlot(u.getUsername(), seat.getId(), start, end);
        Assertions.assertNull(err);

        // second user should be blocked (same seat+same slot conflict, or quota full)
        Users u2 = new Users();
        u2.setUsername("slot_test_user2");
        u2.setPassword("123456");
        userService.addUser(u2);
        err = seatReservationService.reserveSlot(u2.getUsername(), seat.getId(), start, end);
        Assertions.assertNotNull(err);
        Assertions.assertTrue(err.contains("约满") || err.contains("座位已被预约"));
    }
}
