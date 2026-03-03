package com.book.manager.controller;

import com.book.manager.entity.UserProfile;
import com.book.manager.entity.Users;
import com.book.manager.service.UserProfileService;
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

@Tag(name = "用户画像")
@Controller
@RequestMapping("/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserService userService;

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "我的画像页面")
    @GetMapping("/my")
    public String myPage() {
        return "user/user-profile";
    }

    @Operation(summary = "获取我的画像")
    @GetMapping("/my/get")
    @ResponseBody
    public R myGet() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        UserProfile p = userProfileService.getOrCompute(u.getId());
        return R.success(CodeEnum.SUCCESS, p);
    }

    @Operation(summary = "手动重算我的画像（演示/调试）")
    @PostMapping("/my/recompute")
    @ResponseBody
    public R recompute() {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        UserProfile p = userProfileService.computeAndSave(u.getId());
        return R.success(CodeEnum.SUCCESS, p);
    }

    @Operation(summary = "保存我的画像可编辑字段")
    @PostMapping("/my/save")
    @ResponseBody
    public R save(@RequestParam(required = false) String grade,
                  @RequestParam(required = false) String majors,
                  @RequestParam(required = false) String interests) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        UserProfile p = userProfileService.updateEditable(u.getId(), grade, majors, interests);
        return R.success(CodeEnum.SUCCESS, p);
    }
}
