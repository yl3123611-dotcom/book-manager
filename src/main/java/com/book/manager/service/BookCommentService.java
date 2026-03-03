package com.book.manager.service;

import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BookCommentMapper;
import com.book.manager.entity.Users;
import com.book.manager.util.vo.BookCommentOut;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookCommentService {

    @Autowired
    private BookCommentMapper bookCommentMapper;

    @Autowired
    private UserService userService;

    @Transactional
    public boolean add(String username, Integer bookId, String content) {
        Users u = userService.findByUsername(username);
        if (u == null) return false;
        if (bookId == null || bookId <= 0) return false;
        if (content == null) return false;
        String c = content.trim();
        if (c.isEmpty()) return false;
        if (c.length() > 1000) c = c.substring(0, 1000);
        return bookCommentMapper.insert(bookId, u.getId(), c) > 0;
    }

    public PageInfo<BookCommentOut> list(Integer bookId, Integer page, Integer size) {
        int p = (page == null || page <= 0) ? 1 : page;
        int s = (size == null || size <= 0) ? 10 : Math.min(size, 50);
        PageHelper.startPage(p, s);
        List<BookCommentOut> list = bookCommentMapper.listByBook(bookId);
        // 兜底：如 XML 没格式化，仍保留原值
        if (list != null) {
            for (BookCommentOut o : list) {
                if (o != null && o.getCreatedAt() != null) {
                    // no-op
                }
            }
        }
        return new PageInfo<>(list);
    }
}
