package com.book.manager.controller;

import com.book.manager.entity.HomeBanner;
import com.book.manager.service.HomeService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminHomeController {

    @Autowired
    private HomeService homeService;

    // ===== Banner =====

    @GetMapping("/banner/list")
    public R bannerList() {
        return R.success(CodeEnum.SUCCESS, homeService.listAllBanners());
    }

    @PostMapping("/banner/save")
    public R bannerSave(@RequestBody HomeBanner banner) {
        int n = homeService.saveBanner(banner);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @GetMapping("/banner/delete")
    public R bannerDelete(@RequestParam Integer id) {
        int n = homeService.deleteBanner(id);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @GetMapping("/banner/enabled")
    public R bannerEnabled(@RequestParam Integer id, @RequestParam Integer enabled) {
        int n = homeService.setBannerEnabled(id, enabled);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    // ===== Recommend =====

    @GetMapping("/recommend/list")
    public R recommendList() {
        return R.success(CodeEnum.SUCCESS, homeService.listAllRecs());
    }

    @PostMapping("/recommend/add")
    public R recommendAdd(@RequestParam Integer bookId) {
        int n = homeService.addRecommend(bookId);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @GetMapping("/recommend/remove")
    public R recommendRemove(@RequestParam Integer bookId) {
        int n = homeService.removeRecommend(bookId);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }
}
