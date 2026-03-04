package com.book.manager.service;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.SeatMapper;
import com.book.manager.entity.Seat;
import com.book.manager.entity.SeatReservation;
import com.book.manager.entity.SeatReservationSlotQuota;
import com.book.manager.entity.SeatRuleConfig;
import com.book.manager.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class SeatReservationService {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private SeatRuleService seatRuleService;

    @Autowired
    private SeatQuotaService seatQuotaService;

    @Autowired
    private UserService userService;

    /**
     * 按时段预约：自定义 start/end。
     * 规则：futureDaysLimit、min/max duration、dailyLimit、activeLimit、maxOverlapPerSlot、quota.
     */
    @Transactional
    public String reserveSlot(String username, Integer seatId, Date slotStart, Date slotEnd) {
        if (seatId == null || seatId <= 0 || slotStart == null || slotEnd == null) {
            return "参数错误";
        }

        // MySQL DATETIME 默认秒级精度，统一归一化避免毫秒导致配额匹配失败
        slotStart = DateUtil.parse(DateUtil.formatDateTime(slotStart), "yyyy-MM-dd HH:mm:ss");
        slotEnd = DateUtil.parse(DateUtil.formatDateTime(slotEnd), "yyyy-MM-dd HH:mm:ss");

        if (!slotEnd.after(slotStart)) {
            return "结束时间必须晚于开始时间";
        }

        Users user = userService.findByUsername(username);
        if (user == null) {
            return "用户不存在";
        }

        // 封禁检查
        if (user.getSeatBanUntil() != null && new Date().before(user.getSeatBanUntil())) {
            return "您当前处于预约封禁期，无法预约";
        }

        Seat seat = seatMapper.selectSeatById(seatId);
        if (seat == null) {
            return "座位不存在";
        }

        SeatRuleConfig cfg = seatRuleService.getOrInitDefault();

        Date now = new Date();
        Date slotDate = DateUtil.beginOfDay(slotStart);

        // 未来天数限制
        Date maxDate = DateUtil.offsetDay(DateUtil.beginOfDay(now), cfg.getFutureDaysLimit());
        if (slotStart.after(DateUtil.endOfDay(maxDate))) {
            return "超过可预约未来天数限制";
        }

        // 不能预约过去
        if (slotEnd.before(now)) {
            return "不能预约过去的时间段";
        }

        long mins = DateUtil.between(slotStart, slotEnd, DateUnit.MINUTE);
        if (mins < cfg.getMinDurationMinutes()) {
            return "预约时长不能小于" + cfg.getMinDurationMinutes() + "分钟";
        }
        if (mins > cfg.getMaxDurationMinutes()) {
            return "预约时长不能大于" + cfg.getMaxDurationMinutes() + "分钟";
        }

        // 用户限制：每日次数
        int daily = seatMapper.countDailyReservations(user.getId(), slotDate);
        if (daily >= cfg.getDailyLimitPerUser()) {
            return "超过每日最大预约次数";
        }

        // 用户限制：同时有效预约
        int active = seatMapper.countActiveSlotReservationsByUser(user.getId());
        if (active >= cfg.getActiveLimitPerUser()) {
            return "您已有有效预约，请先取消/结束后再预约";
        }

        // 座位冲突
        int overlaps = seatMapper.countSeatOverlaps(seatId, slotDate, slotStart, slotEnd);
        if (overlaps >= cfg.getMaxOverlapPerSlot()) {
            return "该时间段座位已被预约";
        }

        // 配额检查（按阅览室）
        SeatReservationSlotQuota quota = seatQuotaService.find(slotDate, slotStart, slotEnd, seat.getRoom());
        if (quota == null) {
            return "该阅览室此时间段未配置配额，无法预约";
        }

        // 先原子占用配额，再写预约记录，避免并发超卖
        if (!seatQuotaService.incrementIfAvailable(quota.getId())) {
            return "该时间段已约满";
        }

        // 建立预约
        SeatReservation r = new SeatReservation();
        r.setSeatId(seatId);
        r.setUserId(user.getId());
        r.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        r.setStartTime(now);
        r.setEndTime(null);
        r.setStatus(1); // 1待签到
        r.setSlotDate(slotDate);
        r.setSlotStart(slotStart);
        r.setSlotEnd(slotEnd);
        r.setCheckinDeadlineAt(DateUtil.offsetMinute(slotStart, cfg.getCheckinGraceMinutes()));

        seatMapper.insertSlotReservation(r);

        return null; // success
    }

    @Transactional
    public String cancelSlot(String username, Integer reservationId) {
        if (reservationId == null || reservationId <= 0) {
            return "参数错误";
        }
        Users user = userService.findByUsername(username);
        if (user == null) {
            return "用户不存在";
        }

        // 取预约记录用于回写 quota
        SeatReservation target = null;
        for (SeatReservation r : seatMapper.selectMySlotReservations(user.getId())) {
            if (r.getId() != null && r.getId().equals(reservationId)) {
                target = r;
                break;
            }
        }
        if (target == null) {
            return "预约记录不存在";
        }

        int updated = seatMapper.cancelReservation(reservationId, user.getId());
        if (updated <= 0) {
            return "当前状态无法取消";
        }

        // quota - 1 (min 0)
        Seat seat = seatMapper.selectSeatById(target.getSeatId());
        if (seat != null && target.getSlotDate() != null && target.getSlotStart() != null && target.getSlotEnd() != null) {
            SeatReservationSlotQuota quota = seatQuotaService.find(target.getSlotDate(), target.getSlotStart(), target.getSlotEnd(), seat.getRoom());
            if (quota != null) {
                seatQuotaService.decrementReserved(quota.getId());
            }
        }

        return null;
    }
}
