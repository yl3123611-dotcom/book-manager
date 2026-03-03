package com.book.manager.controller;

import com.book.manager.entity.Notification;
import com.book.manager.entity.Users;
import com.book.manager.service.NotificationService;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "通知")
@Controller
@RequestMapping("/notify")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    @Operation(summary = "通知页面")
    @GetMapping("/list")
    public String page() {
        return "notify/notify-list";
    }

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "我的通知列表")
    @GetMapping("/my")
    @ResponseBody
    public R my(@RequestParam(required = false) Integer status) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        List<Notification> list = notificationService.list(u.getId(), status);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unreadCount")
    @ResponseBody
    public R unreadCount() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, notificationService.unreadCount(u.getId()));
    }

    @Operation(summary = "标记已读")
    @PostMapping("/read")
    @ResponseBody
    public R read(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.markRead(u.getId(), id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @Operation(summary = "全部标记已读")
    @PostMapping("/readAll")
    @ResponseBody
    public R readAll() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        notificationService.markAllRead(u.getId());
        return R.success(CodeEnum.SUCCESS);
    }

    @Operation(summary = "手动触发生成借阅提醒（管理员/调试）")
    @PostMapping("/genBorrow")
    @ResponseBody
    public R genBorrow(@RequestParam(defaultValue = "1") Integer dueDays) {
        int created = notificationService.generateBorrowNotifications(dueDays);
        return R.success(CodeEnum.SUCCESS, created);
    }
}
