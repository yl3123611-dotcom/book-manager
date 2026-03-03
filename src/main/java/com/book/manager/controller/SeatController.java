package com.book.manager.controller;

import com.book.manager.dao.SeatMapper;
import com.book.manager.entity.ReadingRoom;
import com.book.manager.entity.Seat;
import com.book.manager.entity.SeatReservation;
import com.book.manager.entity.Users;
import com.book.manager.service.UserService;
import com.book.manager.service.SeatQuotaService;
import com.book.manager.util.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/seat")
public class SeatController {

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private com.book.manager.service.SeatReservationService seatReservationService;

    @Autowired
    private SeatQuotaService seatQuotaService;

    // ==================== 页面跳转 ====================

    /**
     * 跳转到座位列表页面（用户预约用）
     */
    @GetMapping("/list")
    public String seatList(Model model) {
        List<Seat> seats = seatMapper.selectAllSeats();
        List<ReadingRoom> rooms = seatMapper.selectEnabledRooms();
        model.addAttribute("seats", seats);
        model.addAttribute("rooms", rooms);
        return "seat/seat-list";
    }

    /**
     * 我的预约记录页面
     */
    @GetMapping("/my")
    public String myReservations(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = ((UserDetails) principal).getUsername();
        Users user = userService.findByUsername(username);

        // 优先展示按时段预约（包含 slotStart/slotEnd 字段）
        List<SeatReservation> list = seatMapper.selectMySlotReservations(user.getId());
        model.addAttribute("list", list);
        return "seat/my-seat";
    }

    /**
     * 座位管理页面（管理员用）
     */
    @GetMapping("/manage")
    public String seatManage(Model model) {
        List<Seat> seats = seatMapper.selectAllSeats();
        List<ReadingRoom> rooms = seatMapper.selectAllRooms();
        model.addAttribute("seats", seats);
        model.addAttribute("rooms", rooms);
        return "seat/seat-manage";
    }

    /**
     * 阅览室管理页面（管理员用）
     */
    @GetMapping("/room-manage")
    public String roomManage(Model model) {
        List<ReadingRoom> rooms = seatMapper.selectAllRooms();
        // 统计每个阅览室的座位数
        for (ReadingRoom room : rooms) {
            int count = seatMapper.countSeatsByRoom(room.getName());
            room.setSize(count);
        }
        model.addAttribute("rooms", rooms);
        return "seat/room-manage";
    }

    // ==================== 用户功能接口 ====================

    /**
     * 预约座位接口
     */
    @ResponseBody
    @PostMapping("/reserve")
    @org.springframework.transaction.annotation.Transactional
    public R reserve(@RequestParam Integer seatId) {
        // 1. 获取当前登录用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return R.failMsg("请先登录");
        }
        String username = ((UserDetails) principal).getUsername();
        Users user = userService.findByUsername(username);
        if (user == null) {
            return R.failMsg("用户信息不存在，请重新登录");
        }

        // 2. 一人一座：检查用户是否已有未结束预约
        int activeCnt = seatMapper.countActiveReservationByUser(user.getId());
        if (activeCnt > 0) {
            return R.failMsg("您已有未结束的座位预约，请先签退后再预约");
        }

        // 3. 检查座位是否存在
        Seat seat = seatMapper.selectSeatById(seatId);
        if (seat == null) {
            return R.paramError("座位不存在");
        }

        // 4. 并发地占用：只有空闲座位(status=0)才能更新为占用(status=1)
        int updated = seatMapper.updateSeatStatusIfFree(seatId);
        if (updated <= 0) {
            return R.failMsg("该座位已被占用或维护中");
        }

        // 5. 插入预约记录
        SeatReservation reservation = new SeatReservation();
        reservation.setSeatId(seatId);
        reservation.setUserId(user.getId());
        reservation.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
        reservation.setStartTime(new Date());
        reservation.setStatus(1); // 1-预约中/使用中
        seatMapper.insertReservation(reservation);

        return R.successMsg("预约成功，请准时入座");
    }

    /**
     * 释放座位（签退）
     */
    @ResponseBody
    @PostMapping("/leave")
    @org.springframework.transaction.annotation.Transactional
    public R leave(@RequestParam Integer seatId) {
        // 1. 获取当前登录用户
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return R.failMsg("请先登录");
        }
        String username = ((UserDetails) principal).getUsername();
        Users user = userService.findByUsername(username);
        if (user == null) {
            return R.failMsg("用户信息不存在，请重新登录");
        }

        // 2. 校验：必须是本人当前预约的座位才能签退
        SeatReservation active = seatMapper.selectActiveReservation(seatId, user.getId());
        if (active == null) {
            return R.failMsg("未找到您在该座位的有效预约记录");
        }

        // 3. 更新座位为空闲 + 结束预约记录
        seatMapper.updateSeatStatus(seatId, 0);
        seatMapper.endReservationByUser(seatId, user.getId());

        return R.successMsg("已释放座位");
    }

    /**
     * 获取阅览室列表（供前端下拉框使用）
     */
    @ResponseBody
    @GetMapping("/rooms")
    public R getRooms() {
        List<ReadingRoom> rooms = seatMapper.selectEnabledRooms();
        return R.successMsg("获取成功", rooms);
    }

    /**
     * 按时间段预约座位（自定义时段）
     * 参数：seatId, slotStart, slotEnd（格式：yyyy-MM-dd HH:mm:ss）
     */
    @ResponseBody
    @PostMapping("/reserveSlot")
    public R reserveSlot(@RequestParam Integer seatId,
                         @RequestParam String slotStart,
                         @RequestParam String slotEnd) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return R.failMsg("请先登录");
        }
        String username = ((UserDetails) principal).getUsername();

        try {
            java.util.Date start = cn.hutool.core.date.DateUtil.parse(slotStart, "yyyy-MM-dd HH:mm:ss");
            java.util.Date end = cn.hutool.core.date.DateUtil.parse(slotEnd, "yyyy-MM-dd HH:mm:ss");
            String err = seatReservationService.reserveSlot(username, seatId, start, end);
            return err == null ? R.successMsg("预约成功") : R.failMsg(err);
        } catch (Exception e) {
            return R.paramError("时间格式错误，示例：2026-02-27 08:00:00");
        }
    }

    /**
     * 取消按时段预约
     */
    @ResponseBody
    @PostMapping("/cancelSlot")
    public R cancelSlot(@RequestParam Integer reservationId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return R.failMsg("请先登录");
        }
        String username = ((UserDetails) principal).getUsername();

        String err = seatReservationService.cancelSlot(username, reservationId);
        return err == null ? R.successMsg("已取消") : R.failMsg(err);
    }

    /**
     * 查询某阅览室某天已配置的配额时段（供前端下拉选择）
     * 参数：room（阅览室名称），date（yyyy-MM-dd）
     */
    @ResponseBody
    @GetMapping("/quotaSlots")
    public R quotaSlots(@RequestParam String room, @RequestParam String date) {
        if (room == null || room.isBlank()) {
            return R.paramError("room不能为空");
        }
        if (date == null || date.isBlank()) {
            return R.paramError("date不能为空");
        }
        try {
            java.util.Date slotDate = cn.hutool.core.date.DateUtil.parse(date, "yyyy-MM-dd");
            java.util.List<com.book.manager.entity.SeatReservationSlotQuota> all = seatQuotaService.listAll();
            java.util.List<com.book.manager.entity.SeatReservationSlotQuota> out = new java.util.ArrayList<>();
            for (com.book.manager.entity.SeatReservationSlotQuota q : all) {
                if (q == null) continue;
                if (q.getRoom() == null) continue;
                if (!room.equals(q.getRoom())) continue;
                if (q.getSlotDate() == null) continue;
                if (cn.hutool.core.date.DateUtil.isSameDay(q.getSlotDate(), slotDate)) {
                    out.add(q);
                }
            }
            return R.successMsg("获取成功", out);
        } catch (Exception e) {
            return R.paramError("date格式错误，示例：2026-02-27");
        }
    }

    // ==================== 管理员功能接口 ====================

    /**
     * 添加座位
     */
    @ResponseBody
    @PostMapping("/admin/addSeat")
    public R addSeat(@RequestBody Seat seat) {
        if (seat.getSeatNo() == null || seat.getSeatNo().trim().isEmpty()) {
            return R.paramError("座位编号不能为空");
        }
        if (seat.getRoom() == null || seat.getRoom().trim().isEmpty()) {
            return R.paramError("请选择所属阅览室");
        }
        seat.setStatus(0); // 新座位默认空闲
        seatMapper.insertSeat(seat);
        return R.successMsg("添加成功");
    }

    /**
     * 批量添加座位
     */
    @ResponseBody
    @PostMapping("/admin/batchAddSeat")
    public R batchAddSeat(@RequestParam String room,
                          @RequestParam String prefix,
                          @RequestParam Integer startNo,
                          @RequestParam Integer count) {
        if (room == null || room.trim().isEmpty()) {
            return R.paramError("请选择阅览室");
        }
        if (prefix == null || prefix.trim().isEmpty()) {
            return R.paramError("座位前缀不能为空");
        }
        if (count == null || count <= 0 || count > 100) {
            return R.paramError("添加数量应在1-100之间");
        }

        List<Seat> seats = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Seat seat = new Seat();
            seat.setSeatNo(prefix + String.format("%03d", startNo + i));
            seat.setRoom(room);
            seat.setStatus(0);
            seats.add(seat);
        }
        seatMapper.batchInsertSeats(seats);
        return R.successMsg("成功添加 " + count + " 个座位");
    }

    /**
     * 更新座位
     */
    @ResponseBody
    @PostMapping("/admin/updateSeat")
    public R updateSeat(@RequestBody Seat seat) {
        if (seat.getId() == null) {
            return R.paramError("座位ID不能为空");
        }
        seatMapper.updateSeat(seat);
        return R.successMsg("更新成功");
    }

    /**
     * 删除座位
     */
    @ResponseBody
    @PostMapping("/admin/deleteSeat")
    public R deleteSeat(@RequestParam Integer id) {
        // 检查座位是否有正在进行的预约
        int activeCount = seatMapper.countActiveReservationBySeat(id);
        if (activeCount > 0) {
            return R.failMsg("该座位当前有用户正在使用，无法删除");
        }
        seatMapper.deleteSeat(id);
        return R.successMsg("删除成功");
    }

    /**
     * 获取所有座位列表（管理员）
     */
    @ResponseBody
    @GetMapping("/admin/seats")
    public R getAllSeats() {
        List<Seat> seats = seatMapper.selectAllSeats();
        return R.successMsg("获取成功", seats);
    }

    /**
     * 强制释放座位（管理员）
     */
    @ResponseBody
    @PostMapping("/admin/forceRelease")
    @org.springframework.transaction.annotation.Transactional
    public R forceRelease(@RequestParam Integer seatId) {
        seatMapper.updateSeatStatus(seatId, 0);
        seatMapper.endReservation(seatId);
        return R.successMsg("座位已强制释放");
    }

    // ==================== 阅览室管理接口 ====================

    /**
     * 添加阅览室
     */
    @ResponseBody
    @PostMapping("/admin/addRoom")
    public R addRoom(@RequestBody ReadingRoom room) {
        if (room.getName() == null || room.getName().trim().isEmpty()) {
            return R.paramError("阅览室名称不能为空");
        }
        // 检查名称是否已存在
        ReadingRoom exist = seatMapper.selectRoomByName(room.getName());
        if (exist != null) {
            return R.failMsg("阅览室名称已存在");
        }
        if (room.getEnabled() == null) {
            room.setEnabled(1); // 默认启用
        }
        if (room.getSize() == null) {
            room.setSize(0);
        }
        seatMapper.insertRoom(room);
        return R.successMsg("添加成功");
    }

    /**
     * 更新阅览室
     */
    @ResponseBody
    @PostMapping("/admin/updateRoom")
    public R updateRoom(@RequestBody ReadingRoom room) {
        if (room.getId() == null) {
            return R.paramError("阅览室ID不能为空");
        }
        seatMapper.updateRoom(room);
        return R.successMsg("更新成功");
    }

    /**
     * 删除阅览室
     */
    @ResponseBody
    @PostMapping("/admin/deleteRoom")
    public R deleteRoom(@RequestParam Integer id) {
        // 先获取阅览室信息
        ReadingRoom room = seatMapper.selectRoomById(id);
        if (room == null) {
            return R.paramError("阅览室不存在");
        }
        // 检查该阅览室下是否还有座位
        int seatCount = seatMapper.countSeatsByRoom(room.getName());
        if (seatCount > 0) {
            return R.failMsg("该阅览室下还有 " + seatCount + " 个座位，请先删除座位");
        }
        seatMapper.deleteRoom(id);
        return R.successMsg("删除成功");
    }

    /**
     * 获取所有阅览室列表（管理员）
     */
    @ResponseBody
    @GetMapping("/admin/rooms")
    public R getAllRooms() {
        List<ReadingRoom> rooms = seatMapper.selectAllRooms();
        return R.successMsg("获取成功", rooms);
    }

    /**
     * 切换阅览室启用状态
     */
    @ResponseBody
    @PostMapping("/admin/toggleRoom")
    public R toggleRoom(@RequestParam Integer id) {
        ReadingRoom room = seatMapper.selectRoomById(id);
        if (room == null) {
            return R.paramError("阅览室不存在");
        }
        room.setEnabled(room.getEnabled() == 1 ? 0 : 1);
        seatMapper.updateRoom(room);
        return R.successMsg(room.getEnabled() == 1 ? "已启用" : "已禁用");
    }
}
