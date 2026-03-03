package com.book.manager.dao;

import com.book.manager.entity.SeatReservationSlotQuota;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;

@Mapper
public interface SeatReservationSlotQuotaMapper {

    @Select("SELECT * FROM seat_reservation_slot_quota ORDER BY slot_date DESC, slot_start ASC")
    List<SeatReservationSlotQuota> listAll();

    @Select("SELECT * FROM seat_reservation_slot_quota WHERE slot_date=#{slotDate} AND slot_start=#{slotStart} AND slot_end=#{slotEnd} AND room=#{room} LIMIT 1")
    SeatReservationSlotQuota find(@Param("slotDate") Date slotDate,
                                  @Param("slotStart") Date slotStart,
                                  @Param("slotEnd") Date slotEnd,
                                  @Param("room") String room);

    @Insert("INSERT INTO seat_reservation_slot_quota (slot_date, slot_start, slot_end, room, reserved_count, max_count) VALUES (#{slotDate}, #{slotStart}, #{slotEnd}, #{room}, #{reservedCount}, #{maxCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SeatReservationSlotQuota q);

    @Update("UPDATE seat_reservation_slot_quota SET reserved_count=#{reservedCount}, max_count=#{maxCount} WHERE id=#{id}")
    int update(SeatReservationSlotQuota q);

    @Update("UPDATE seat_reservation_slot_quota SET reserved_count=#{reservedCount} WHERE id=#{id}")
    int updateReservedCount(SeatReservationSlotQuota q);

    @Delete("DELETE FROM seat_reservation_slot_quota WHERE id=#{id}")
    int delete(@Param("id") Long id);
}
