package com.book.manager.controller;

import com.book.manager.entity.SeatReservationSlotQuota;
import com.book.manager.entity.SeatRuleConfig;
import com.book.manager.service.SeatQuotaService;
import com.book.manager.service.SeatRuleService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/seat/rule")
public class SeatRuleController {

    @Autowired
    private SeatRuleService seatRuleService;

    @Autowired
    private SeatQuotaService seatQuotaService;

    @GetMapping("/config")
    public String pageConfig() {
        return "seat/seat-rule-config";
    }

    @GetMapping("/quota")
    public String pageQuota() {
        return "seat/seat-slot-quota";
    }

    @GetMapping("/get")
    @ResponseBody
    public R get() {
        return R.success(CodeEnum.SUCCESS, seatRuleService.getOrInitDefault());
    }

    @PostMapping("/save")
    @ResponseBody
    public R save(@RequestBody SeatRuleConfig cfg) {
        boolean ok = seatRuleService.save(cfg);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @GetMapping("/quota/list")
    @ResponseBody
    public R quotaList() {
        List<SeatReservationSlotQuota> list = seatQuotaService.listAll();
        return R.success(CodeEnum.SUCCESS, list);
    }

    @PostMapping("/quota/save")
    @ResponseBody
    public R quotaSave(@RequestBody SeatReservationSlotQuota q) {
        boolean ok = seatQuotaService.save(q);
        return ok ? R.success(CodeEnum.SUCCESS) : R.paramError("参数错误");
    }

    @PostMapping("/quota/delete")
    @ResponseBody
    public R quotaDelete(@RequestParam Long id) {
        boolean ok = seatQuotaService.delete(id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }
}
