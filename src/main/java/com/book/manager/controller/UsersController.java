package com.book.manager.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.book.manager.entity.Users;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.consts.Constants;
import com.book.manager.util.consts.ConvertUtil;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.ro.PageIn;
import com.book.manager.util.vo.PageOut;
import com.book.manager.util.vo.UserOut;
import com.github.pagehelper.PageInfo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
public class UsersController {

    @Autowired
    private UserService userService;

    // ✅ 注入 application.yml 中的上传路径 (D:/biyesheji/upload/)
    @Value("${file.upload-dir}")
    private String uploadDir;

    /* ====================== 用户管理 ====================== */

    /**
     * ✅ 兼容前端 GET /user/list?size=10
     */
    @Operation(summary = "用户列表(GET兼容)")
    @GetMapping("/list")
    public R getUsersGet(@RequestParam(required = false) Integer size) {
        PageIn pageIn = new PageIn();
        pageIn.setCurrPage(1);
        pageIn.setPageSize(size != null ? size : 10);
        pageIn.setKeyword("");
        return getUsers(pageIn, size);
    }

    /**
     * ✅ 原 POST /user/list 保留，但允许 body 为空（避免 400）
     */
    @Operation(summary = "用户列表")
    @PostMapping("/list")
    public R getUsers(@RequestBody(required = false) PageIn pageIn,
                      @RequestParam(required = false) Integer size) {
        PageIn finalIn = normalizePageIn(pageIn, size);
        PageInfo<Users> userList = userService.getUserList(finalIn);
        return R.success(CodeEnum.SUCCESS, buildPageOut(userList));
    }

    @Operation(summary = "读者列表")
    @PostMapping("/readerList")
    public R getReaders(@RequestBody(required = false) PageIn pageIn,
                        @RequestParam(required = false) Integer size) {
        PageIn finalIn = normalizePageIn(pageIn, size);
        PageInfo<Users> userList = userService.getReaderList(finalIn);
        return R.success(CodeEnum.SUCCESS, buildPageOut(userList));
    }

    @Operation(summary = "添加用户")
    @PostMapping("/add")
    public R addUsers(@RequestBody Users users) {
        return R.success(CodeEnum.SUCCESS, userService.addUser(users));
    }

    @Operation(summary = "添加读者")
    @PostMapping("/addReader")
    public R addReader(@RequestBody Users users) {
        if (users == null || StrUtil.isBlank(users.getUsername()) || StrUtil.isBlank(users.getPassword())) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        users.setIsAdmin(1);
        if (users.getSize() == null || users.getSize() <= 0) {
            users.setSize(5);
        }
        if (users.getIdentity() == null) {
            users.setIdentity(0);
        }
        return R.success(CodeEnum.SUCCESS, userService.addUser(users));
    }

    @Operation(summary = "添加管理员")
    @PostMapping("/addAdmin")
    public R addAdmin(@RequestBody Users users) {
        if (users == null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        users.setIsAdmin(0);
        return R.success(CodeEnum.SUCCESS, userService.addUser(users));
    }

    @Operation(summary = "编辑用户")
    @PostMapping("/update")
    public R modifyUsers(@RequestBody Users users) {
        if (users == null || users.getId() == null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        if (users.getSize() != null && users.getSize() <= 0) {
            return R.failMsg("可借数量必须大于0");
        }
        return R.success(CodeEnum.SUCCESS, userService.updateUser(users));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/detail")
    public R userDetail(@RequestParam Integer id) {
        Users user = userService.findUserById(id);
        if (user != null) {
            UserOut out = new UserOut();
            BeanUtils.copyProperties(user, out);
            out.setBirth(DateUtil.format(user.getBirthday(), Constants.DATE_FORMAT));
            out.setIdent(ConvertUtil.identStr(user.getIdentity()));
            return R.success(CodeEnum.SUCCESS, out);
        }
        return R.fail(CodeEnum.NOT_FOUND);
    }

    @Operation(summary = "删除用户")
    @GetMapping("/delete")
    public R delUsers(@RequestParam Integer id) {
        userService.deleteUser(id);
        return R.success(CodeEnum.SUCCESS);
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/currUser")
    public R getCurrUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal != null) {
            Map<String, Object> map = BeanUtil.beanToMap(principal);
            String username = (String) map.get("username");
            if (StrUtil.isNotBlank(username)) {
                Users users = userService.findByUsername(username);
                if (users == null) {
                    return R.fail(CodeEnum.USER_NOT_FOUND);
                }

                UserOut out = new UserOut();
                BeanUtils.copyProperties(users, out);
                out.setBirth(DateUtil.format(users.getBirthday(), Constants.DATE_FORMAT));
                out.setIdent(ConvertUtil.identStr(users.getIdentity()));

                return R.success(CodeEnum.SUCCESS, out);
            }
        }
        return R.fail(CodeEnum.USER_NOT_FOUND);
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R register(@RequestBody Users users) {
        if (users == null || StrUtil.isBlank(users.getUsername()) || StrUtil.isBlank(users.getPassword())) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        Users exist = userService.findByUsername(users.getUsername());
        if (exist != null) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        users.setIsAdmin(1);
        return R.success(CodeEnum.SUCCESS, userService.addUser(users));
    }

    /* ====================== 头像上传（已修正） ====================== */

    @Operation(summary = "上传用户头像")
    @PostMapping("/uploadAvatar")
    public R uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return R.failMsg("请选择要上传的头像");
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                return R.failMsg("头像大小不能超过 5MB");
            }

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null ||
                    !(originalFilename.endsWith(".jpg")
                            || originalFilename.endsWith(".png")
                            || originalFilename.endsWith(".jpeg"))) {
                return R.failMsg("仅支持 jpg / png / jpeg 格式");
            }

            // 使用配置的 uploadDir，确保路径以 / 结尾
            String basePath = uploadDir.endsWith("/") ? uploadDir : uploadDir + "/";
            // 拼接到 avatar 子目录：D:/biyesheji/upload/avatar/
            String realPath = basePath + "avatar/";

            File dir = new File(realPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 生成文件名
            String fileName = System.currentTimeMillis() + "_" + originalFilename;
            File dest = new File(dir, fileName);

            // 保存文件
            file.transferTo(dest);

            // 返回 URL：浏览器访问路径必须以 /upload/ 开头
            String avatarUrl = "/upload/avatar/" + fileName;
            return R.successMsg("上传成功", avatarUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return R.failMsg("头像上传失败：" + e.getMessage());
        }
    }

    private PageIn normalizePageIn(PageIn pageIn, Integer size) {
        if (pageIn != null) {
            if (pageIn.getCurrPage() == null || pageIn.getCurrPage() <= 0) {
                pageIn.setCurrPage(1);
            }
            if (pageIn.getPageSize() == null || pageIn.getPageSize() <= 0) {
                pageIn.setPageSize(size != null ? size : 10);
            }
            if (pageIn.getKeyword() == null) {
                pageIn.setKeyword("");
            }
            return pageIn;
        }

        PageIn finalIn = new PageIn();
        finalIn.setCurrPage(1);
        finalIn.setPageSize(size != null ? size : 10);
        finalIn.setKeyword("");
        return finalIn;
    }

    private PageOut buildPageOut(PageInfo<Users> userList) {
        PageOut pageOut = new PageOut();
        pageOut.setCurrPage(userList.getPageNum());
        pageOut.setPageSize(userList.getPageSize());
        pageOut.setTotal((int) userList.getTotal());

        List<UserOut> outs = new ArrayList<>();
        for (Users users : userList.getList()) {
            UserOut out = new UserOut();
            BeanUtils.copyProperties(users, out);
            out.setIdent(ConvertUtil.identStr(users.getIdentity()));
            out.setBirth(DateUtil.format(users.getBirthday(), Constants.DATE_FORMAT));
            outs.add(out);
        }
        pageOut.setList(outs);
        return pageOut;
    }
}
