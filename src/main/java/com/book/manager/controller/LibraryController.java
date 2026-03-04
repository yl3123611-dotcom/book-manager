package com.book.manager.controller;

import com.book.manager.service.LibraryService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/nav")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @GetMapping("/find-book")
    public String findBookView(){
        return "nav/find-book";
    }

    @GetMapping("/floors")
    @ResponseBody
    public R floors(){
        List<Map<String,Object>> list = libraryService.listFloors();
        return R.success(CodeEnum.SUCCESS, list);
    }

    @GetMapping("/shelves")
    @ResponseBody
    public R shelves(@RequestParam Integer floorId){
        List<Map<String,Object>> list = libraryService.listShelves(floorId);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @GetMapping("/cells")
    @ResponseBody
    public R cells(@RequestParam Integer shelfId){
        List<Map<String,Object>> list = libraryService.listShelfCells(shelfId);
        return R.success(CodeEnum.SUCCESS, list);
    }

    @GetMapping("/book-location")
    @ResponseBody
    public R bookLocation(@RequestParam Integer bookId){
        Map<String,Object> loc = libraryService.findBookLocation(bookId);
        return R.success(CodeEnum.SUCCESS, loc);
    }

    @PostMapping("/admin/bind")
    @ResponseBody
    public R bind(@RequestParam Integer bookId, @RequestParam Integer floorId, @RequestParam Integer shelfId, @RequestParam Integer cellId){
        boolean ok = libraryService.bindBookLocation(bookId, floorId, shelfId, cellId);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("绑定失败");
    }

    @GetMapping("/admin/manage")
    public String adminManage(){
        return "nav/admin-manage";
    }

    @GetMapping("/floor-3d")
    public String floor3d(){
        return "nav/floor-3d";
    }

    @GetMapping("/shelf/books")
    @ResponseBody
    public R shelfBooks(@RequestParam Integer shelfId){
        return R.success(CodeEnum.SUCCESS, libraryService.listBooksByShelf(shelfId));
    }

    @PostMapping("/admin/shelf/add")
    @ResponseBody
    public R addShelf(@RequestParam Integer floorId, @RequestParam String code, @RequestParam(required = false) String name,
                      @RequestParam(required = false) Integer x, @RequestParam(required = false) Integer y){
        boolean ok = libraryService.insertShelf(floorId, code, name, x, y, null);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加书架失败");
    }

    @PostMapping("/admin/cell/add")
    @ResponseBody
    public R addCell(@RequestParam Integer shelfId, @RequestParam Integer layerNo, @RequestParam Integer cellNo,
                     @RequestParam(required = false) Integer x, @RequestParam(required = false) Integer y){
        boolean ok = libraryService.insertCell(shelfId, layerNo, cellNo, x, y);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加格位失败");
    }

    @GetMapping("/admin/floors")
    @ResponseBody
    public R adminFloors(){
        return R.success(CodeEnum.SUCCESS, libraryService.adminListFloors());
    }

    @PostMapping("/admin/floor/add")
    @ResponseBody
    public R addFloor(@RequestParam String name,
                      @RequestParam(required = false) Integer sort,
                      @RequestParam(required = false) String mapImageUrl){
        boolean ok = libraryService.insertFloor(name, sort == null ? 0 : sort, mapImageUrl);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加楼层失败");
    }
}
