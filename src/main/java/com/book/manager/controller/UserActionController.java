package com.book.manager.controller;

import com.book.manager.entity.Users;
import com.book.manager.service.UserActionService;
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

@Tag(name = "用户行为")
@Controller
@RequestMapping("/actions")
public class UserActionController {

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private UserService userService;

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "我的行为页面")
    @GetMapping("/my")
    public String myPage() {
        return "user/user-actions";
    }

    @Operation(summary = "我的行为列表")
    @GetMapping("/my/list")
    @ResponseBody
    public R myList(@RequestParam(required = false) Integer limit) {
        Users u = currentUser();
        if (u == null) return R.fail(CodeEnum.USER_NOT_FOUND);
        return R.success(CodeEnum.SUCCESS, userActionService.listMy(u.getId(), limit));
    }
}
