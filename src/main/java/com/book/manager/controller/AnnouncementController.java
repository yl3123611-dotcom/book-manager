package com.book.manager.controller;

import com.book.manager.entity.Announcement;
import com.book.manager.entity.Users;
import com.book.manager.service.AnnouncementService;
import com.book.manager.service.UserService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "公告")
@Controller
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private UserService userService;

    private Users currentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails ud) {
            return userService.findByUsername(ud.getUsername());
        }
        return null;
    }

    @Operation(summary = "公告列表页面")
    @GetMapping("/list")
    public String listPage() {
        return "announcement/announcement-list";
    }

    @Operation(summary = "公告管理页面")
    @GetMapping("/admin/manage")
    public String adminManagePage() {
        return "announcement/announcement-admin-manage";
    }

    @Operation(summary = "首页公告(置顶/最新)")
    @GetMapping("/top")
    @ResponseBody
    public R top(@RequestParam(defaultValue = "3") Integer limit) {
        List<Announcement> list = announcementService.top(limit == null ? 3 : limit);
        return R.success(CodeEnum.SUCCESS, list);
    }

    // ===== Admin CRUD =====

    @Operation(summary = "公告分页列表(后台)")
    @GetMapping("/admin/page")
    @ResponseBody
    public R adminPage(@RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer size) {
        try {
            Map<String, Object> data = announcementService.page(page == null ? 1 : page, size == null ? 10 : size);
            return R.success(CodeEnum.SUCCESS, data);
        } catch (Exception e) {
            return R.failMsg("分页加载失败：" + e.getMessage());
        }
    }

    @Operation(summary = "公告详情(后台)")
    @GetMapping("/admin/detail")
    @ResponseBody
    public R adminDetail(@RequestParam Integer id) {
        try {
            return R.success(CodeEnum.SUCCESS, announcementService.detail(id));
        } catch (Exception e) {
            return R.failMsg("读取详情失败：" + e.getMessage());
        }
    }

    @Operation(summary = "公告保存(新增/编辑)")
    @PostMapping("/admin/save")
    @ResponseBody
    public R adminSave(@RequestBody Announcement a) {
        try {
            Users u = currentUser();
            Integer uid = (u == null) ? null : u.getId();
            int n = announcementService.save(a, uid);
            return n > 0 ? R.success(CodeEnum.SUCCESS, a) : R.fail(CodeEnum.FAIL);
        } catch (Exception e) {
            return R.failMsg("保存失败：" + e.getMessage());
        }
    }

    @Operation(summary = "公告删除")
    @RequestMapping(value = "/admin/delete", method = {RequestMethod.GET, RequestMethod.POST})
    @ResponseBody
    public R adminDelete(@RequestParam Integer id) {
        try {
            int n = announcementService.delete(id);
            if (n > 0) {
                return R.success(CodeEnum.SUCCESS);
            } else {
                return R.failMsg("未找到需要删除的公告或已被删除");
            }
        } catch (org.springframework.dao.DataAccessException dae) {
            // likely DB constraint or other DB-level error
            return R.failMsg("删除失败，数据库错误或存在关联数据");
        } catch (Exception e) {
            return R.failMsg("删除失败：" + e.getMessage());
        }
    }

    @Operation(summary = "公告启用/禁用")
    @GetMapping("/admin/enabled")
    @ResponseBody
    public R adminEnabled(@RequestParam Integer id, @RequestParam Integer enabled) {
        int n = announcementService.setEnabled(id, enabled);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }

    @Operation(summary = "公告置顶/取消置顶")
    @GetMapping("/admin/pin")
    @ResponseBody
    public R adminPin(@RequestParam Integer id, @RequestParam Integer pinned) {
        int n = announcementService.setPinned(id, pinned);
        return n > 0 ? R.success(CodeEnum.SUCCESS) : R.fail(CodeEnum.FAIL);
    }
}
