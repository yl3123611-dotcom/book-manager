package com.book.manager.controller;

import com.book.manager.service.HotSearchService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
public class SearchController {

    @Autowired
    private HotSearchService hotSearchService;

    @GetMapping("/hot")
    public R hot(@RequestParam(defaultValue = "10") Integer size) {
        return R.success(CodeEnum.SUCCESS, hotSearchService.listTop(size));
    }
}
