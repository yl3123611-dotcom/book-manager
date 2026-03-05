package com.book.manager.controller;

import cn.hutool.extra.qrcode.QrCodeUtil;
import cn.hutool.extra.qrcode.QrConfig;
import com.book.manager.entity.Users;
import com.book.manager.service.SpecialTopicService;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/topic")
public class SpecialTopicController {

    @Autowired
    private SpecialTopicService service;

    @Autowired
    private UserService userService;

    @GetMapping("/{slug}")
    public String topicView(@PathVariable String slug) {
        return "special/topic-view";
    }

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @GetMapping("/api/{slug}/books")
    @ResponseBody
    public R topicBooks(@PathVariable String slug) {
        Map<String, Object> topic = service.findBySlug(slug);
        if (topic == null) return R.fail(CodeEnum.NOT_FOUND);
        Integer tid = (Integer) topic.get("id");
        List<Map<String, Object>> books = service.listTopicBooks(tid);
        return R.success(CodeEnum.SUCCESS, books);
    }

    @GetMapping("/api/list")
    @ResponseBody
    public R topicList() {
        return R.success(CodeEnum.SUCCESS, service.listTopics());
    }

    @GetMapping("/admin/list")
    @ResponseBody
    public R adminTopicList() {
        return R.success(CodeEnum.SUCCESS, service.listTopics());
    }

    @GetMapping("/api/{slug}")
    @ResponseBody
    public R topicBySlug(@PathVariable String slug) {
        Map<String, Object> topic = service.findBySlug(slug);
        return topic != null ? R.success(CodeEnum.SUCCESS, topic) : R.fail(CodeEnum.NOT_FOUND);
    }

    @GetMapping("/api/{slug}/related")
    @ResponseBody
    public R related(@PathVariable String slug, @RequestParam(required = false) Integer bookId) {
        return R.success(CodeEnum.SUCCESS, service.relatedTopicsBySlug(slug, bookId));
    }

    @PostMapping("/api/{slug}/favorite")
    @ResponseBody
    public R favorite(@PathVariable String slug, @RequestParam Integer bookId,
                      @RequestParam(defaultValue = "true") Boolean on) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = service.favoriteBySlug(slug, bookId, u.getId(), Boolean.TRUE.equals(on));
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("收藏操作失败");
    }

    @GetMapping("/api/{slug}/favorite/status")
    @ResponseBody
    public R favoriteStatus(@PathVariable String slug, @RequestParam Integer bookId) {
        Users u = currentUser();
        if (u == null) return R.success(CodeEnum.SUCCESS, false);
        return R.success(CodeEnum.SUCCESS, service.isFavoritedBySlug(slug, bookId, u.getId()));
    }

    @PostMapping("/api/{slug}/share")
    @ResponseBody
    public R share(@PathVariable String slug,
                   @RequestParam(required = false) Integer bookId,
                   @RequestParam(required = false) String channel,
                   @RequestParam(required = false) String content) {
        Users u = currentUser();
        Integer uid = u == null ? null : u.getId();
        String shareText = service.saveShareAndBuildQrContent(slug, bookId, uid, channel, content);
        if (shareText == null) {
            return R.fail(CodeEnum.NOT_FOUND);
        }
        String qrBase64 = QrCodeUtil.generateAsBase64(shareText, new QrConfig(220, 220), "png");
        Map<String, Object> data = new HashMap<>();
        data.put("shareText", shareText);
        data.put("qrBase64", qrBase64);
        return R.success(CodeEnum.SUCCESS, data);
    }

    @GetMapping("/api/book/{bookId}")
    @ResponseBody
    public R topicsByBook(@PathVariable Integer bookId) {
        return R.success(CodeEnum.SUCCESS, service.listTopicsByBookId(bookId));
    }

    @GetMapping("/admin/stats")
    @ResponseBody
    public R stats(@RequestParam(required = false) Integer topicId) {
        return R.success(CodeEnum.SUCCESS, service.stats(topicId));
    }

    // ===== Admin APIs =====

    @PostMapping("/admin/add")
    @ResponseBody
    public R adminAdd(@RequestParam String slug, @RequestParam String name, @RequestParam(required = false) String description) {
        boolean ok = service.insertTopic(slug, name, description);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加专题失败");
    }

    @PostMapping("/admin/addBook")
    @ResponseBody
    public R adminAddBook(@RequestParam Integer topicId, @RequestParam Integer bookId, @RequestParam(required = false) Integer sort) {
        boolean ok = service.addTopicBook(topicId, bookId, sort == null ? 0 : sort);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加专题书籍失败");
    }

    @PostMapping("/admin/removeBook")
    @ResponseBody
    public R adminRemoveBook(@RequestParam Integer topicId, @RequestParam Integer bookId) {
        boolean ok = service.removeTopicBook(topicId, bookId);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("移除失败");
    }

    @PostMapping("/admin/dangshi/toggle")
    @ResponseBody
    public R adminToggleDangshi(@RequestParam Integer bookId) {
        String slug = "dangshi-hongshu";
        boolean has = false;
        List<Map<String, Object>> topics = service.listTopicsByBookId(bookId);
        if (topics != null) {
            for (Map<String, Object> t : topics) {
                if (slug.equals(String.valueOf(t.get("slug")))) {
                    has = true;
                    break;
                }
            }
        }
        boolean ok = has ? service.removeBookFromTopicSlug(slug, bookId) : service.addBookToTopicSlug(slug, bookId, 0);
        if (!ok) return R.failMsg("操作失败");
        return R.success(CodeEnum.SUCCESS, !has);
    }
}
