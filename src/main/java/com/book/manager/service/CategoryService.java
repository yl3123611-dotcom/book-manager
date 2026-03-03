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

    public List<Map<String,Object>> listEnabled(){
        return categoryMapper.listEnabled();
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

