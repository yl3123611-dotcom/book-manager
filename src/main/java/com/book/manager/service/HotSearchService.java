package com.book.manager.service;

import com.book.manager.dao.HotSearchMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class HotSearchService {

    @Autowired
    private HotSearchMapper hotSearchMapper;

    @Transactional
    public void record(String keyword) {
        if (keyword == null) return;
        String k = keyword.trim();
        if (k.isEmpty()) return;
        // 简单防刷：限制长度
        if (k.length() > 50) k = k.substring(0, 50);
        hotSearchMapper.upsertIncrement(k);
    }

    public List<String> listTop(Integer size) {
        int s = (size == null || size <= 0) ? 10 : Math.min(size, 50);
        return hotSearchMapper.listTop(s);
    }
}
