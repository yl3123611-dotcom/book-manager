package com.book.manager.controller;

import com.book.manager.entity.ReadingReport;
import com.book.manager.entity.Users;
import com.book.manager.service.ReadingReportService;
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

@Tag(name = "阅读报告")
@Controller
@RequestMapping("/report")
public class ReadingReportController {

    @Autowired
    private ReadingReportService readingReportService;

    @Autowired
    private UserService userService;

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "我的阅读报告页面")
    @GetMapping("/my")
    public String myPage() {
        return "user/reading-report";
    }

    @Operation(summary = "获取我的阅读报告（week/month）")
    @GetMapping("/my/get")
    @ResponseBody
    public R myGet(@RequestParam(defaultValue = "week") String period) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        ReadingReport r = readingReportService.getOrBuild(u.getId(), period);
        return R.success(CodeEnum.SUCCESS, r);
    }

    @Operation(summary = "手动生成/更新我的阅读报告（演示）")
    @PostMapping("/my/build")
    @ResponseBody
    public R myBuild(@RequestParam(defaultValue = "week") String period) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        ReadingReport r = readingReportService.getOrBuild(u.getId(), period);
        return R.success(CodeEnum.SUCCESS, r);
    }
}
