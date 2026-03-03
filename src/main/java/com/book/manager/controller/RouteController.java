package com.book.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @Description 路由
 */
@Tag(name = "路由")
@Controller
public class RouteController {

    /**
     * 跳转登录
     */
    @Operation(summary = "跳转登录页")
    @GetMapping({"/login", "/", "/logout"})
    public String toLogin() {
        return "login";
    }

    /**
     * 跳转注册页
     */
    @Operation(summary = "跳转注册页")
    @GetMapping("/register")
    public String toRegister() {
        return "register";
    }

    /**
     * 跳转首页
     */
    @Operation(summary = "跳转首页")
    @RequestMapping("/index")
    public String toIndex() {
        return "index";
    }

    /**
     * 跳转欢迎页面
     */
    @Operation(summary = "跳转欢迎页面")
    @RequestMapping("/welcome")
    public String toWelcome() {
        return "welcome";
    }

    /**
     * 好书推荐页面
     */
    @Operation(summary = "跳转好书推荐页面")
    @GetMapping({"/recommend", "/recommend.html"})
    public String toRecommend() {
        return "recommend";
    }

    /**
     * ✅ 二级路由跳转
     * ✅ 关键：name 不允许包含 “.”，避免把 /upload/*.png、/*.js、/*.css 当成页面模板处理
     */
    @Operation(summary = "二级路由跳转")
    @RequestMapping("/{filename}/{name:[^\\.]+}")
    public String change(@PathVariable String filename,
                         @PathVariable String name) {
        return filename + "/" + name;
    }
}

