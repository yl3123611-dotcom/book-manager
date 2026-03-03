package com.book.manager.dao;

import com.book.manager.entity.SeatRuleConfig;
import org.apache.ibatis.annotations.*;

@Mapper
public interface SeatRuleConfigMapper {

    @Select("SELECT * FROM seat_rule_config LIMIT 1")
    SeatRuleConfig get();

    @Insert("INSERT INTO seat_rule_config (future_days_limit, min_duration_minutes, max_duration_minutes, daily_limit_per_user, active_limit_per_user, checkin_grace_minutes, temp_leave_max_minutes, max_overlap_per_slot, violation_limit, ban_days) " +
            "VALUES (#{futureDaysLimit}, #{minDurationMinutes}, #{maxDurationMinutes}, #{dailyLimitPerUser}, #{activeLimitPerUser}, #{checkinGraceMinutes}, #{tempLeaveMaxMinutes}, #{maxOverlapPerSlot}, #{violationLimit}, #{banDays})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeatRuleConfig cfg);

    @Update("UPDATE seat_rule_config SET future_days_limit=#{futureDaysLimit}, min_duration_minutes=#{minDurationMinutes}, max_duration_minutes=#{maxDurationMinutes}, daily_limit_per_user=#{dailyLimitPerUser}, active_limit_per_user=#{activeLimitPerUser}, checkin_grace_minutes=#{checkinGraceMinutes}, temp_leave_max_minutes=#{tempLeaveMaxMinutes}, max_overlap_per_slot=#{maxOverlapPerSlot}, violation_limit=#{violationLimit}, ban_days=#{banDays} WHERE id=#{id}")
    int update(SeatRuleConfig cfg);
}
