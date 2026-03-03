package com.book.manager.controller;

import com.book.manager.service.ForumService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.ro.PageIn;
import com.book.manager.util.vo.PageOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forum/admin")
public class ForumAdminController {

    @Autowired
    private ForumService forumService;

    @GetMapping("/manage")
    public String manageView() {
        return "forum/admin-manage";
    }

    @GetMapping("/list")
    @ResponseBody
    public R list(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = forumService.adminList(keyword);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @PostMapping("/list/page")
    @ResponseBody
    public R listPage(@RequestBody(required = false) PageIn pageIn) {
        if (pageIn == null) {
            pageIn = new PageIn();
            pageIn.setCurrPage(1);
            pageIn.setPageSize(20);
            pageIn.setKeyword("");
        }
        PageOut out = forumService.getAdminPage(pageIn);
        return R.success(CodeEnum.SUCCESS, out);
    }

    @PostMapping("/status")
    @ResponseBody
    public R status(@RequestParam Long id, @RequestParam Integer status) {
        boolean ok = forumService.updateStatus(id, status);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("更新状态失败");
    }

    @PostMapping("/pin")
    @ResponseBody
    public R pin(@RequestParam Long id, @RequestParam Integer isPinned) {
        boolean ok = forumService.updatePinned(id, isPinned);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("置顶失败");
    }

    @PostMapping("/feature")
    @ResponseBody
    public R feature(@RequestParam Long id, @RequestParam Integer isFeatured) {
        boolean ok = forumService.updateFeatured(id, isFeatured);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("加精失败");
    }

    @GetMapping("/replies")
    @ResponseBody
    public R replies(@RequestParam Long postId) {
        List<Map<String, Object>> list = forumService.listReplies(postId);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @PostMapping("/reply/status")
    @ResponseBody
    public R replyStatus(@RequestParam Long id, @RequestParam Integer status) {
        boolean ok = forumService.updateReplyStatus(id, status);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("回复状态更新失败");
    }

    @PostMapping("/post/recount")
    @ResponseBody
    public R recount(@RequestParam Long postId){
        boolean ok = forumService.recountReplyCount(postId);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("重算失败");
    }
}
