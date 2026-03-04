package com.book.manager.service;

import com.book.manager.dao.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    private static final List<String> DEFAULT_CATEGORY_NAMES = List.of(
            "文学", "历史", "科技", "计算机", "心理学", "艺术", "教育", "经济", "管理", "政治法律",
            "哲学", "医学", "自然科学", "外语", "军事"
    );

    public List<Map<String,Object>> listEnabled(){
        return categoryMapper.listEnabled();
    }

    public List<String> listDefaultNames() {
        return DEFAULT_CATEGORY_NAMES;
    }

    public void ensureExists(String name) {
        if (name == null) return;
        String n = name.trim();
        if (n.isEmpty()) return;
        if (n.length() > 50) {
            n = n.substring(0, 50);
        }
        Map<String, Object> existed = categoryMapper.findByName(n);
        if (existed == null) {
            categoryMapper.insertIgnore(n, 999);
        }
    }

    // admin
    public List<Map<String,Object>> adminList(){
        return categoryMapper.adminList();
    }

    public boolean insert(String name, Integer sort){
        return categoryMapper.insert(name, sort) > 0;
    }

    public boolean update(Integer id, String name, Integer sort, Integer enabled){
        return categoryMapper.update(id, name, sort, enabled) > 0;
    }

    public boolean delete(Integer id){
        return categoryMapper.delete(id) > 0;
    }
}
