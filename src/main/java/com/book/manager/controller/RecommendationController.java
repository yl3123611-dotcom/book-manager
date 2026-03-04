package com.book.manager.controller;

import com.book.manager.entity.Users;
import com.book.manager.service.RecommendationService;
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

@Tag(name = "推荐系统")
@Controller
@RequestMapping("/recommend")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private UserService userService;

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "获取我的推荐（优先缓存）")
    @GetMapping("/my")
    @ResponseBody
    public R my() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, recommendationService.getCachedRecs(u.getId()));
    }

    @Operation(summary = "刷新我的推荐（重算并写缓存/明细）")
    @PostMapping("/refresh")
    @ResponseBody
    public R refresh(@RequestParam(defaultValue = "5") Integer size) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, recommendationService.refresh(u.getId(), size));
    }

    @Operation(summary = "我的推荐历史页面")
    @GetMapping("/history-view")
    public String historyView() {
        return "book/recommend-history";
    }

    @Operation(summary = "获取我的推荐历史")
    @GetMapping("/history")
    @ResponseBody
    public R history(@RequestParam(required = false) Integer limit) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, recommendationService.history(u.getId(), limit));
    }
}
