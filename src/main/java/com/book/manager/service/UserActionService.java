package com.book.manager.service;

import com.book.manager.dao.UserActionMapper;
import com.book.manager.entity.UserAction;
import com.book.manager.entity.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserActionService {

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private UserService userService;

    public void record(String username, String actionType, Integer bookId, Integer score) {
        if (username == null || username.isBlank()) return;
        if (actionType == null || actionType.isBlank()) return;

        Users u = userService.findByUsername(username);
        if (u == null) return;

        UserAction a = new UserAction();
        a.setUserId(u.getId());
        a.setActionType(actionType);
        a.setBookId(bookId);
        a.setScore(score);
        userActionMapper.insert(a);
    }

    public void recordView(String username, Integer bookId) {
        record(username, "VIEW", bookId, 1);
    }

    public void recordBorrow(String username, Integer bookId) {
        record(username, "BORROW", bookId, 5);
    }

    public void recordReturn(String username, Integer bookId) {
        record(username, "RETURN", bookId, 3);
    }

    public void recordRenew(String username, Integer bookId) {
        record(username, "RENEW", bookId, 2);
    }

    public List<UserAction> listMy(Integer userId, Integer limit) {
        if (userId == null) return List.of();
        if (limit == null || limit <= 0) limit = 50;
        return userActionMapper.listByUser(userId, limit);
    }
}
