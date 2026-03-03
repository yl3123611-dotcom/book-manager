package com.book.manager.controller;

import com.book.manager.service.ForumService;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.entity.Users;
import com.book.manager.util.ro.PageIn;
import com.book.manager.util.vo.PageOut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    @GetMapping("/post-list")
    public String listView() {
        return "forum/post-list";
    }

    @GetMapping("/post-add")
    public String addView() {
        return "forum/post-add";
    }

    @GetMapping("/post-detail")
    public String detailView() {
        return "forum/post-detail";
    }

    @GetMapping("/api/posts")
    @ResponseBody
    public R posts(@RequestParam(required = false) String keyword) {
        List<Map<String, Object>> list = forumService.listPosts(keyword);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @GetMapping("/api/post")
    @ResponseBody
    public R post(@RequestParam Long id) {
        Map<String, Object> data = forumService.getPost(id);
        if (data == null) return R.fail(CodeEnum.NOT_FOUND);
        forumService.incView(id);
        return R.success(CodeEnum.SUCCESS, data);
    }

    @PostMapping("/api/post/add")
    @ResponseBody
    public R add(@RequestParam(required = false) Integer userId, @RequestParam String title, @RequestParam String content) {
        // prefer current authenticated user
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer uid = null;
        if (principal instanceof UserDetails ud) {
            Users u = userService.findByUsername(ud.getUsername());
            if (u != null) uid = u.getId();
        }
        if (userId != null && uid == null) uid = userId;

        if (uid == null) return R.failMsg("请先登录");
        boolean ok = forumService.addPost(uid, title, content);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("发帖失败，检查参数");
    }

    @GetMapping("/api/replies")
    @ResponseBody
    public R replies(@RequestParam Long postId) {
        List<Map<String, Object>> list = forumService.listReplies(postId);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @PostMapping("/api/reply/add")
    @ResponseBody
    public R addReply(@RequestParam Long postId, @RequestParam String content) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer uid = null;
        if (principal instanceof UserDetails ud) {
            Users u = userService.findByUsername(ud.getUsername());
            if (u != null) uid = u.getId();
        }
        if (uid == null) return R.failMsg("请先登录");
        boolean ok = forumService.addReply(postId, uid, content);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("回复失败");
    }

    @PostMapping("/api/posts/page")
    @ResponseBody
    public R postsPage(@RequestBody PageIn pageIn){
        PageOut out = forumService.getPostPage(pageIn);
        return R.success(CodeEnum.SUCCESS, out);
    }
}
