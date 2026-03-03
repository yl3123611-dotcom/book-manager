package com.book.manager.controller;

import com.book.manager.service.HomeService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/banners")
    public R banners() {
        return R.success(CodeEnum.SUCCESS, homeService.getEnabledBanners());
    }

    @GetMapping("/recommends")
    public R recommends(@RequestParam(defaultValue = "8") Integer size) {
        return R.success(CodeEnum.SUCCESS, homeService.getEnabledRecommends(size));
    }
}
