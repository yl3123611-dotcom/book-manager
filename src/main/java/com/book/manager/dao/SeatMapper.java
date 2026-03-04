package com.book.manager.dao;

import com.book.manager.entity.ReadingRoom;
import com.book.manager.entity.Seat;
import com.book.manager.entity.SeatReservation;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface SeatMapper {

    // ==================== 座位查询 ====================

    // 查询所有座位
    @Select("SELECT * FROM seat")
    List<Seat> selectAllSeats();

    // 根据ID查询座位
    @Select("SELECT * FROM seat WHERE id = #{id}")
    Seat selectSeatById(Integer id);

    // 根据阅览室查询座位
    @Select("SELECT * FROM seat WHERE room = #{room}")
    List<Seat> selectSeatsByRoom(String room);

    // ==================== 座位状态管理 ====================

    // 更新座位状态
    @Update("UPDATE seat SET status = #{status} WHERE id = #{id}")
    int updateSeatStatus(@Param("id") Integer id, @Param("status") Integer status);

    // 尝试占用座位（仅当座位当前为空闲 status=0 时才更新为占用 status=1）
    @Update("UPDATE seat SET status = 1 WHERE id = #{id} AND status = 0")
    int updateSeatStatusIfFree(@Param("id") Integer id);

    // ==================== 座位管理（管理员） ====================

    // 添加座位
    @Insert("INSERT INTO seat (seat_no, room, status) VALUES (#{seatNo}, #{room}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSeat(Seat seat);

    // 更新座位信息
    @Update("UPDATE seat SET seat_no = #{seatNo}, room = #{room} WHERE id = #{id}")
    int updateSeat(Seat seat);

    // 删除座位
    @Delete("DELETE FROM seat WHERE id = #{id}")
    int deleteSeat(Integer id);

    // 检查座位是否有正在进行的预约（1待签到 2使用中 3暂离）
    @Select("SELECT COUNT(1) FROM seat_reservation WHERE seat_id = #{seatId} AND status IN (1,2,3)")
    int countActiveReservationBySeat(@Param("seatId") Integer seatId);

    // 批量添加座位
    @Insert("<script>" +
            "INSERT INTO seat (seat_no, room, status) VALUES " +
            "<foreach collection='seats' item='seat' separator=','>" +
            "(#{seat.seatNo}, #{seat.room}, #{seat.status})" +
            "</foreach>" +
            "</script>")
    int batchInsertSeats(@Param("seats") List<Seat> seats);

    // ==================== 预约管理 ====================

    // 查询用户当前是否存在未结束的预约（1待签到 2使用中 3暂离）
    @Select("SELECT COUNT(1) FROM seat_reservation WHERE user_id = #{userId} AND status IN (1,2,3)")
    int countActiveReservationByUser(@Param("userId") Integer userId);

    // 查询用户在某个座位上的有效预约（1待签到 2使用中 3暂离）
    @Select("SELECT * FROM seat_reservation WHERE seat_id = #{seatId} AND user_id = #{userId} AND status IN (1,2,3) LIMIT 1")
    SeatReservation selectActiveReservation(@Param("seatId") Integer seatId, @Param("userId") Integer userId);

    // 释放座位（结束预约，仅允许预约者本人签退）
    @Update("UPDATE seat_reservation SET status = 4, end_time = NOW() WHERE seat_id = #{seatId} AND user_id = #{userId} AND status IN (1,2,3)")
    int endReservationByUser(@Param("seatId") Integer seatId, @Param("userId") Integer userId);

    // 添加预约记录
    @Insert("INSERT INTO seat_reservation (seat_id, user_id, user_name, start_time, end_time, status) " +
            "VALUES (#{seatId}, #{userId}, #{userName}, #{startTime}, #{endTime}, #{status})")
    int insertReservation(SeatReservation reservation);

    // 查询我的预约
    @Select("SELECT r.*, s.seat_no, s.room FROM seat_reservation r " +
            "LEFT JOIN seat s ON r.seat_id = s.id " +
            "WHERE r.user_id = #{userId} ORDER BY r.create_time DESC")
    List<SeatReservation> selectMyReservations(Integer userId);

    // 释放座位（结束预约）- 兼容原逻辑
    @Update("UPDATE seat_reservation SET status = 4, end_time = NOW() WHERE seat_id = #{seatId} AND status IN (1,2,3)")
    int endReservation(Integer seatId);

    // 查询所有预约记录（管理员用）
    @Select("SELECT r.*, s.seat_no, s.room FROM seat_reservation r " +
            "LEFT JOIN seat s ON r.seat_id = s.id " +
            "ORDER BY r.create_time DESC")
    List<SeatReservation> selectAllReservations();

    // ==================== 阅览室管理 ====================

    // 查询所有阅览室
    @Select("SELECT * FROM reading_room ORDER BY id")
    List<ReadingRoom> selectAllRooms();

    // 查询启用的阅览室
    @Select("SELECT * FROM reading_room WHERE enabled = 1 ORDER BY id")
    List<ReadingRoom> selectEnabledRooms();

    // 根据ID查询阅览室
    @Select("SELECT * FROM reading_room WHERE id = #{id}")
    ReadingRoom selectRoomById(Integer id);

    // 根据名称查询阅览室
    @Select("SELECT * FROM reading_room WHERE name = #{name}")
    ReadingRoom selectRoomByName(String name);

    // 添加阅览室
    @Insert("INSERT INTO reading_room (name, location, enabled, size) VALUES (#{name}, #{location}, #{enabled}, #{size})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertRoom(ReadingRoom room);

    // 更新阅览室
    @Update("UPDATE reading_room SET name = #{name}, location = #{location}, enabled = #{enabled}, size = #{size} WHERE id = #{id}")
    int updateRoom(ReadingRoom room);

    // 删除阅览室
    @Delete("DELETE FROM reading_room WHERE id = #{id}")
    int deleteRoom(Integer id);

    // 查询阅览室下的座位数量
    @Select("SELECT COUNT(1) FROM seat WHERE room = #{roomName}")
    int countSeatsByRoom(@Param("roomName") String roomName);

    // ==================== 按时段预约（slot） ====================

    /**
     * 统计用户在某天的预约次数（排除已取消/已结束）
     */
    @Select("SELECT COUNT(1) FROM seat_reservation WHERE user_id = #{userId} AND slot_date = #{slotDate} AND status IN (1,2,3)")
    int countDailyReservations(@Param("userId") Integer userId, @Param("slotDate") java.util.Date slotDate);

    /**
     * 统计用户当前有效预约数（排除已取消/已结束）
     */
    @Select("SELECT COUNT(1) FROM seat_reservation WHERE user_id = #{userId} AND status IN (1,2,3)")
    int countActiveSlotReservationsByUser(@Param("userId") Integer userId);

    /**
     * 检查同一座位在同一日期同时间段是否冲突（status=1/2/3 视为占用）
     * overlap: existing.start < newEnd AND existing.end > newStart
     */
    @Select("SELECT COUNT(1) FROM seat_reservation WHERE seat_id = #{seatId} AND slot_date = #{slotDate} AND status IN (1,2,3) AND slot_start < #{slotEnd} AND slot_end > #{slotStart}")
    int countSeatOverlaps(@Param("seatId") Integer seatId,
                          @Param("slotDate") java.util.Date slotDate,
                          @Param("slotStart") java.util.Date slotStart,
                          @Param("slotEnd") java.util.Date slotEnd);

    /** 查询我的按时段预约（用于我的预约页展示） */
    @Select("SELECT r.*, s.seat_no, s.room FROM seat_reservation r LEFT JOIN seat s ON r.seat_id = s.id WHERE r.user_id = #{userId} ORDER BY r.create_time DESC")
    List<SeatReservation> selectMySlotReservations(Integer userId);

    /**
     * 取消预约（仅允许本人取消，且仅取消待签到/使用中/暂离）
     */
    @Update("UPDATE seat_reservation SET status = 5, end_time = NOW() WHERE id = #{id} AND user_id = #{userId} AND status IN (1,2,3)")
    int cancelReservation(@Param("id") Integer id, @Param("userId") Integer userId);

    /**
     * 新增按时段预约记录
     */
    @Insert("INSERT INTO seat_reservation (seat_id, user_id, user_name, start_time, end_time, status, slot_date, slot_start, slot_end, checkin_deadline_at) " +
            "VALUES (#{seatId}, #{userId}, #{userName}, #{startTime}, #{endTime}, #{status}, #{slotDate}, #{slotStart}, #{slotEnd}, #{checkinDeadlineAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertSlotReservation(SeatReservation reservation);
}
