package com.book.manager.controller;

import com.book.manager.service.CategoryService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @ResponseBody
    public R list() {
        List<Map<String,Object>> list = categoryService.listEnabled();
        return R.success(CodeEnum.SUCCESS, list);
    }

    // admin endpoints (optional)
    @GetMapping("/admin/list")
    @ResponseBody
    public R adminList(){
        return R.success(CodeEnum.SUCCESS, categoryService.adminList());
    }

    @PostMapping("/admin/add")
    @ResponseBody
    public R add(@RequestParam String name, @RequestParam(required = false) Integer sort){
        boolean ok = categoryService.insert(name, sort == null ? 0 : sort);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加失败");
    }

    @PostMapping("/admin/update")
    @ResponseBody
    public R update(@RequestParam Integer id, @RequestParam String name, @RequestParam(required = false) Integer sort, @RequestParam(required = false) Integer enabled){
        boolean ok = categoryService.update(id, name, sort == null ? 0 : sort, enabled == null ? 1 : enabled);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("更新失败");
    }

    @PostMapping("/admin/delete")
    @ResponseBody
    public R delete(@RequestParam Integer id){
        boolean ok = categoryService.delete(id);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("删除失败");
    }
}

