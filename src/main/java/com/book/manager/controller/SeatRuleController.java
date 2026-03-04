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

import java.util.Date;
import java.util.Map;
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
    public R quotaSave(@RequestBody Map<String, Object> body) {
        try {
            SeatReservationSlotQuota q = new SeatReservationSlotQuota();
            q.setId(parseLong(body.get("id")));
            q.setSlotDate(parseDate(body.get("slotDate"), "yyyy-MM-dd"));
            q.setSlotStart(parseDate(body.get("slotStart"), "yyyy-MM-dd HH:mm:ss"));
            q.setSlotEnd(parseDate(body.get("slotEnd"), "yyyy-MM-dd HH:mm:ss"));
            q.setRoom(readText(body.get("room")));
            q.setMaxCount(parseInteger(body.get("maxCount")));

            boolean ok = seatQuotaService.save(q);
            return ok ? R.success(CodeEnum.SUCCESS) : R.paramError("参数错误或保存失败");
        } catch (Exception e) {
            return R.paramError("时间格式错误，请使用 yyyy-MM-dd HH:mm:ss");
        }
    }

    @PostMapping("/quota/delete")
    @ResponseBody
    public R quotaDelete(@RequestParam Long id) {
        boolean ok = seatQuotaService.delete(id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    private String readText(Object value) {
        return value == null ? null : String.valueOf(value).trim();
    }

    private Integer parseInteger(Object value) {
        if (value == null) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value).trim());
    }

    private Long parseLong(Object value) {
        if (value == null || String.valueOf(value).trim().isEmpty()) {
            return null;
        }
        return Long.parseLong(String.valueOf(value).trim());
    }

    private Date parseDate(Object value, String pattern) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        if ("yyyy-MM-dd HH:mm:ss".equals(pattern)) {
            text = text.replace("T", " ");
            if (text.length() == 16) {
                text = text + ":00";
            }
        }
        return cn.hutool.core.date.DateUtil.parse(text, pattern);
    }
}
