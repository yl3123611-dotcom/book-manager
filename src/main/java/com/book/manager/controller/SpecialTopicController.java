package com.book.manager.controller;

import com.book.manager.service.SpecialTopicService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/topic")
public class SpecialTopicController {

    @Autowired
    private SpecialTopicService service;

    @GetMapping("/{slug}")
    public String topicView(@PathVariable String slug){
        // view resolved by template special/topic-view (front-end reads slug from path)
        return "special/topic-view";
    }

    @GetMapping("/api/{slug}/books")
    @ResponseBody
    public R topicBooks(@PathVariable String slug){
        Map<String,Object> topic = service.findBySlug(slug);
        if(topic == null) return R.fail(CodeEnum.NOT_FOUND);
        Integer tid = (Integer) topic.get("id");
        List<Map<String,Object>> books = service.listTopicBooks(tid);
        return R.success(CodeEnum.SUCCESS, books);
    }

    @GetMapping("/api/list")
    @ResponseBody
    public R topicList(){
        return R.success(CodeEnum.SUCCESS, service.listTopics());
    }

    @GetMapping("/api/{slug}")
    @ResponseBody
    public R topicBySlug(@PathVariable String slug){
        Map<String,Object> topic = service.findBySlug(slug);
        return topic != null ? R.success(CodeEnum.SUCCESS, topic) : R.fail(CodeEnum.NOT_FOUND);
    }

    @GetMapping("/api/book/{bookId}")
    @ResponseBody
    public R topicsByBook(@PathVariable Integer bookId){
        return R.success(CodeEnum.SUCCESS, service.listTopicsByBookId(bookId));
    }

    // ===== Admin APIs =====
    @GetMapping("/admin/list")
    @ResponseBody
    public R adminList(){
        return R.success(CodeEnum.SUCCESS, service.listTopics());
    }

    @PostMapping("/admin/add")
    @ResponseBody
    public R adminAdd(@RequestParam String slug, @RequestParam String name, @RequestParam(required = false) String description){
        boolean ok = service.insertTopic(slug, name, description);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加专题失败");
    }

    @PostMapping("/admin/addBook")
    @ResponseBody
    public R adminAddBook(@RequestParam Integer topicId, @RequestParam Integer bookId, @RequestParam(required = false) Integer sort){
        boolean ok = service.addTopicBook(topicId, bookId, sort == null ? 0 : sort);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("添加专题书籍失败");
    }

    @PostMapping("/admin/removeBook")
    @ResponseBody
    public R adminRemoveBook(@RequestParam Integer topicId, @RequestParam Integer bookId){
        boolean ok = service.removeTopicBook(topicId, bookId);
        return ok ? R.success(CodeEnum.SUCCESS) : R.failMsg("移除失败");
    }

    /**
     * 党史红书：管理员一键加入/移除
     */
    @PostMapping("/admin/dangshi/toggle")
    @ResponseBody
    public R adminToggleDangshi(@RequestParam Integer bookId){
        String slug = "dangshi-hongshu";
        // 已在专题内则移除，否则加入
        boolean has = false;
        List<Map<String,Object>> topics = service.listTopicsByBookId(bookId);
        if(topics != null){
            for(Map<String,Object> t : topics){
                if(slug.equals(String.valueOf(t.get("slug")))){
                    has = true;
                    break;
                }
            }
        }
        boolean ok = has ? service.removeBookFromTopicSlug(slug, bookId) : service.addBookToTopicSlug(slug, bookId, 0);
        if(!ok) return R.failMsg("操作失败");
        return R.success(CodeEnum.SUCCESS, !has);
    }
}
