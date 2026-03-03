package com.book.manager.service;

import com.book.manager.dao.AnnouncementMapper;
import com.book.manager.entity.Announcement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    public List<Announcement> top(int limit) {
        if (limit <= 0) limit = 3;
        if (limit > 20) limit = 20;
        return announcementMapper.selectTop(limit);
    }

    public Map<String, Object> page(int page, int size) {
        if (page <= 0) page = 1;
        if (size <= 0) size = 10;
        if (size > 50) size = 50;
        int total = announcementMapper.countAll();
        int offset = (page - 1) * size;
        List<Announcement> list = announcementMapper.listPage(offset, size);
        Map<String, Object> out = new HashMap<>();
        out.put("total", total);
        out.put("page", page);
        out.put("size", size);
        out.put("list", list);
        return out;
    }

    public List<Announcement> listAll() {
        return announcementMapper.listAll();
    }

    public Announcement detail(Integer id) {
        if (id == null) return null;
        return announcementMapper.selectById(id);
    }

    public int save(Announcement a, Integer userId) {
        if (a == null) return 0;
        if (a.getEnabled() == null) a.setEnabled(1);
        if (a.getPinned() == null) a.setPinned(0);
        if (a.getPublishTime() == null) a.setPublishTime(LocalDateTime.now());
        if (a.getId() == null) {
            a.setCreatedBy(userId);
            return announcementMapper.insert(a);
        }
        return announcementMapper.update(a);
    }

    public int delete(Integer id) {
        if (id == null) return 0;
        return announcementMapper.deleteById(id);
    }

    public int setEnabled(Integer id, Integer enabled) {
        if (id == null) return 0;
        if (enabled == null) enabled = 1;
        return announcementMapper.setEnabled(id, enabled);
    }

    public int setPinned(Integer id, Integer pinned) {
        if (id == null) return 0;
        if (pinned == null) pinned = 0;
        return announcementMapper.setPinned(id, pinned);
    }
}
