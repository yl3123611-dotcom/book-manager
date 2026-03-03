package com.book.manager.entity;

import lombok.Data;
import java.util.Date;

@Data
public class SeatReservation {
    private Integer id;
    private Integer seatId;
    private Integer userId;
    private String userName;
    private Date startTime;
    private Date endTime;
    private Integer status; // 1-预约中
    private Date createTime;

    // 冗余字段用于展示
    private String seatNo;
    private String room;

    // ====== 按时段预约（与 seat_reservation 表字段对齐）======
    private Date slotDate;
    private Date slotStart;
    private Date slotEnd;
    private Date checkinDeadlineAt;
    private Date checkedInAt;
    private Date tempLeaveUntil;
    private Integer noShowCount;
}