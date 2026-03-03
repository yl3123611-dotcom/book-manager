package com.book.manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/special/admin")
public class SpecialAdminController {

    @GetMapping("/manage")
    public String manage(){
        return "special/admin-manage";
    }
}
