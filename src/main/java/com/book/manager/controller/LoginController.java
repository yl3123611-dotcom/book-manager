package com.book.manager.controller;

import cn.hutool.core.util.StrUtil;
import com.book.manager.entity.Users;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping("/auth")
public class LoginController {

    @Autowired
    private UserService userService;

    /**
     * 示例：注册接口（避免与你 /user/register 冲突，这里用 /auth/register）
     */
    @ResponseBody
    @PostMapping("/register")
    public R register(@RequestBody Users users) {
        if (users == null || StrUtil.isBlank(users.getUsername()) || StrUtil.isBlank(users.getPassword())) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        Users exist = userService.findByUsername(users.getUsername());
        if (exist != null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        users.setIsAdmin(1);
        return R.success(CodeEnum.SUCCESS, userService.addUser(users));
    }

    /**
     * 示例：退出后跳转（其实 Security 已处理 logoutSuccessUrl("/")，这个一般也不需要）
     */
    @GetMapping("/logoutSuccess")
    public void logoutSuccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect("/");
    }
}
