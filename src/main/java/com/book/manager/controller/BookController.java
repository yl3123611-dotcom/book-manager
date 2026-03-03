package com.book.manager.controller;

import com.book.manager.entity.Book;
import com.book.manager.service.BookService;
import com.book.manager.service.UserActionService;
import com.book.manager.service.HotSearchService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.ro.PageIn;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name = "图书管理")
@Controller
@RequestMapping("/book")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserActionService userActionService;

    @Autowired
    private HotSearchService hotSearchService;

    // ✅ 从 application.yml 读取上传目录，避免硬编码
    @Value("${file.upload-dir}")
    private String uploadDir;

    // ==================== 页面跳转视图 ====================

    @Operation(summary = "图书列表视图")
    @GetMapping("/book-list")
    public String bookList() {
        return "book/book-list";
    }

    @Operation(summary = "图书添加视图")
    @GetMapping("/book-add")
    public String bookAdd() {
        return "book/book-add";
    }

    @Operation(summary = "图书更新视图")
    @GetMapping("/book-update")
    public String bookUpdate() {
        return "book/book-update";
    }

    @Operation(summary = "图书详情视图")
    @GetMapping("/detail-view")
    public String bookDetailView() {
        return "book/book-detail";
    }

    // ==================== 数据接口 API ====================

    @Operation(summary = "图书搜索列表")
    @PostMapping("/list")
    @ResponseBody
    public R getBookList(@RequestBody PageIn pageIn) {
        if (pageIn == null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        // ✅ 记录热门搜索
        hotSearchService.record(pageIn.getKeyword());
        return R.success(CodeEnum.SUCCESS, bookService.getBookList(pageIn));
    }

    @Operation(summary = "添加图书")
    @PostMapping("/add")
    @ResponseBody
    public R addBook(@RequestBody Book book) {
        return R.success(CodeEnum.SUCCESS, bookService.addBook(book));
    }

    @Operation(summary = "编辑图书")
    @PostMapping("/update")
    @ResponseBody
    public R modifyBook(@RequestBody Book book) {
        return R.success(CodeEnum.SUCCESS, bookService.updateBook(book));
    }

    @Operation(summary = "图书详情")
    @GetMapping("/detail")
    @ResponseBody
    public R bookDetail(@RequestParam Integer id) {
        // 记录浏览行为（登录用户）
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            userActionService.recordView(ud.getUsername(), id);
        }
        return R.success(CodeEnum.SUCCESS, bookService.findBookById(id));
    }

    @Operation(summary = "图书详情（根据 ISBN 获取）")
    @GetMapping("/detailByIsbn")
    @ResponseBody
    public R bookDetailByIsbn(@RequestParam String isbn) {
        return R.success(CodeEnum.SUCCESS, bookService.findBookByIsbn(isbn));
    }

    @Operation(summary = "删除图书")
    @GetMapping("/delete")
    @ResponseBody
    public R delBook(@RequestParam Integer id) {
        bookService.deleteBook(id);
        return R.success(CodeEnum.SUCCESS);
    }

    @Operation(summary = "图片上传接口")
    @PostMapping("/upload")
    @ResponseBody
    public R upload(@RequestParam("file") MultipartFile file) {

        // 0. 基础校验
        if (file == null || file.isEmpty()) {
            return R.failMsg("请选择要上传的图片");
        }

        // 1. 获取文件名和后缀
        String fileName = file.getOriginalFilename();
        String suffixName = "";
        if (fileName != null && fileName.lastIndexOf(".") != -1) {
            suffixName = fileName.substring(fileName.lastIndexOf("."));
        }

        // 2. 生成新文件名
        String newFileName = UUID.randomUUID().toString().replace("-", "") + suffixName;

        // 3. 真实保存路径（来自 yml）
        String realUploadDir = ensureEndsWithSlash(uploadDir);

        File dir = new File(realUploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File dest = new File(dir, newFileName);

        try {
            // 4. 保存文件
            file.transferTo(dest);

            // 5. 返回访问 URL（必须与 WebConfig 的 /upload/** 对齐）
            String url = "/upload/" + newFileName;
            return R.success(CodeEnum.SUCCESS, url);

        } catch (IOException e) {
            e.printStackTrace();
            return R.fail(CodeEnum.UPLOAD_ERROR);
        }
    }

    private String ensureEndsWithSlash(String path) {
        if (path == null || path.isBlank()) return path;
        return path.endsWith("/") ? path : path + "/";
    }
}
