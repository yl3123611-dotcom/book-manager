package com.book.manager.controller;

import com.book.manager.service.BookCommentService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.ro.BookCommentIn;
import com.github.pagehelper.PageInfo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class BookCommentController {

    @Autowired
    private BookCommentService bookCommentService;

    @GetMapping("/list")
    public R list(@RequestParam Integer bookId,
                  @RequestParam(defaultValue = "1") Integer page,
                  @RequestParam(defaultValue = "10") Integer size) {
        PageInfo<?> info = bookCommentService.list(bookId, page, size);
        return R.success(CodeEnum.SUCCESS, info);
    }

    @PostMapping("/add")
    public R add(@RequestBody @Valid BookCommentIn in) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (!(principal instanceof UserDetails ud)) {
            return R.failMsg("请先登录");
        }
        boolean ok = bookCommentService.add(ud.getUsername(), in.getBookId(), in.getContent());
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("评论失败");
    }
}
