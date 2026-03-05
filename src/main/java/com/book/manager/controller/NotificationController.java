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
import java.util.Map;

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

    @Operation(summary = "归档页")
    @GetMapping("/archivePage")
    public String archivePage() {
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

    @Operation(summary = "我的归档通知")
    @GetMapping("/archived")
    @ResponseBody
    public R archived() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, notificationService.archivedList(u.getId()));
    }

    @Operation(summary = "通知详情")
    @GetMapping("/detail")
    @ResponseBody
    public R detail(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        Notification n = notificationService.detail(u.getId(), id);
        return n != null ? R.success(CodeEnum.SUCCESS, n) : R.fail(CodeEnum.NOT_FOUND);
    }

    @Operation(summary = "已读回执")
    @GetMapping("/receipt")
    @ResponseBody
    public R receipt(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        List<Map<String, Object>> rows = notificationService.readReceipt(u.getId(), id);
        return R.success(CodeEnum.SUCCESS, rows);
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

    @Operation(summary = "签收通知")
    @PostMapping("/sign")
    @ResponseBody
    public R sign(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.sign(u.getId(), id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("签收失败");
    }

    @Operation(summary = "回复通知")
    @PostMapping("/reply")
    @ResponseBody
    public R reply(@RequestParam Integer id, @RequestParam String replyContent) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.reply(u.getId(), id, replyContent);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("回复失败");
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

    @Operation(summary = "归档通知")
    @PostMapping("/archive")
    @ResponseBody
    public R archive(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.archive(u.getId(), id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @Operation(summary = "恢复归档通知")
    @PostMapping("/unarchive")
    @ResponseBody
    public R unarchive(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.unarchive(u.getId(), id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @Operation(summary = "删除通知")
    @PostMapping("/delete")
    @ResponseBody
    public R delete(@RequestParam Integer id) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        boolean ok = notificationService.remove(u.getId(), id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @Operation(summary = "手动触发生成借阅提醒（管理员/调试）")
    @PostMapping("/genBorrow")
    @ResponseBody
    public R genBorrow(@RequestParam(defaultValue = "1") Integer dueDays) {
        int created = notificationService.generateBorrowNotifications(dueDays);
        return R.success(CodeEnum.SUCCESS, created);
    }
}
