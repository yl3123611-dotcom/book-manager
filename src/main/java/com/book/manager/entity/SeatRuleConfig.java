package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "座位预约规则配置")
public class SeatRuleConfig {

    private Integer id;

    @Schema(description = "最多可预约未来N天")
    private Integer futureDaysLimit;

    @Schema(description = "最短预约时长(分钟)")
    private Integer minDurationMinutes;

    @Schema(description = "最长预约时长(分钟)")
    private Integer maxDurationMinutes;

    @Schema(description = "每日最大预约次数")
    private Integer dailyLimitPerUser;

    @Schema(description = "同时最多有效预约数")
    private Integer activeLimitPerUser;

    @Schema(description = "开始后允许签到的宽限(分钟)")
    private Integer checkinGraceMinutes;

    @Schema(description = "暂离最长时间(分钟)")
    private Integer tempLeaveMaxMinutes;

    @Schema(description = "同一时间段同一座位最多被预约数")
    private Integer maxOverlapPerSlot;

    @Schema(description = "违约次数上限")
    private Integer violationLimit;

    @Schema(description = "违约封禁天数")
    private Integer banDays;

    private Date updatedAt;
}
