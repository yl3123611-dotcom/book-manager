package com.book.manager.controller;

import com.book.manager.entity.Users;
import com.book.manager.service.ForumService;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
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

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @GetMapping("/api/posts")
    @ResponseBody
    public R posts(@RequestParam(required = false) String keyword,
                   @RequestParam(required = false) String category) {
        List<Map<String, Object>> list = forumService.listPosts(keyword, category);
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
    public R add(@RequestParam(required = false) Integer userId,
                 @RequestParam String title,
                 @RequestParam String content,
                 @RequestParam(required = false) String category) {
        Users u = currentUser();
        Integer uid = u == null ? null : u.getId();
        if (userId != null && uid == null) uid = userId;

        if (uid == null) return R.failMsg("请先登录");
        boolean ok = forumService.addPost(uid, title, content, category);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("发帖失败，检查参数");
    }

    @PostMapping("/api/post/update")
    @ResponseBody
    public R updatePost(@RequestParam Long id,
                        @RequestParam String title,
                        @RequestParam String content,
                        @RequestParam(required = false) String category) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.updateMyPost(id, u.getId(), title, content, category);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("更新失败，仅支持编辑自己的帖子");
    }

    @PostMapping("/api/post/delete")
    @ResponseBody
    public R deletePost(@RequestParam Long id) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.deleteMyPost(id, u.getId());
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("删除失败，仅支持删除自己的帖子");
    }

    @PostMapping("/api/post/report")
    @ResponseBody
    public R reportPost(@RequestParam Long postId,
                        @RequestParam(required = false) String reason) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.reportPost(postId, u.getId(), reason);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("举报失败");
    }

    @GetMapping("/api/categories")
    @ResponseBody
    public R categories() {
        return R.success(CodeEnum.SUCCESS, List.of("问答", "闲聊", "资源", "求助", "综合"));
    }

    @GetMapping("/api/replies")
    @ResponseBody
    public R replies(@RequestParam Long postId) {
        List<Map<String, Object>> list = forumService.listReplies(postId);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @PostMapping("/api/reply/add")
    @ResponseBody
    public R addReply(@RequestParam Long postId, @RequestParam String content,
                      @RequestParam(required = false) Long parentId) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.addReply(postId, u.getId(), content, parentId);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("回复失败");
    }

    @PostMapping("/api/post/favorite")
    @ResponseBody
    public R favorite(@RequestParam Long postId, @RequestParam(defaultValue = "true") Boolean on) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.favorite(postId, u.getId(), Boolean.TRUE.equals(on));
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("操作失败");
    }

    @GetMapping("/api/post/favorite/status")
    @ResponseBody
    public R favoriteStatus(@RequestParam Long postId) {
        Users u = currentUser();
        if (u == null) return R.success(CodeEnum.SUCCESS, false);
        return R.success(CodeEnum.SUCCESS, forumService.isFavorited(postId, u.getId()));
    }

    @PostMapping("/api/post/accept")
    @ResponseBody
    public R accept(@RequestParam Long postId, @RequestParam Long replyId) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        boolean ok = forumService.acceptReply(postId, replyId, u.getId());
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("采纳失败");
    }

    @GetMapping("/api/my/posts")
    @ResponseBody
    public R myPosts(@RequestParam(required = false) String keyword) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        return R.success(CodeEnum.SUCCESS, forumService.myPosts(u.getId(), keyword));
    }

    @GetMapping("/api/my/favorites")
    @ResponseBody
    public R myFavorites(@RequestParam(required = false) String keyword) {
        Users u = currentUser();
        if (u == null) return R.failMsg("请先登录");
        return R.success(CodeEnum.SUCCESS, forumService.myFavorites(u.getId(), keyword));
    }

    @PostMapping("/api/posts/page")
    @ResponseBody
    public R postsPage(@RequestBody PageIn pageIn) {
        PageOut out = forumService.getPostPage(pageIn);
        return R.success(CodeEnum.SUCCESS, out);
    }
}
