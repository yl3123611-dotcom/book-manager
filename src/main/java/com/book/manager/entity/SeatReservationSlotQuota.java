package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "座位预约时间段配额")
public class SeatReservationSlotQuota {

    private Long id;

    private Date slotDate;

    private Date slotStart;

    private Date slotEnd;

    private String room;

    private Integer reservedCount;

    private Integer maxCount;

    private Date updatedAt;
}
