package com.book.manager.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "专题")
@Controller
@RequestMapping("/special")
public class SpecialController {

    @Operation(summary = "红楼梦专区视图")
    @GetMapping("/hongloumeng")
    public String hongloumeng() {
        return "special/hongloumeng";
    }

    @Operation(summary = "专题管理页面")
    @GetMapping("/admin/manage")
    public String adminManage() {
        return "special/admin-manage";
    }
}
