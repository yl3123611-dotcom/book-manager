package com.book.manager.service;

import cn.hutool.core.util.StrUtil;
import com.book.manager.dao.BorrowMapper;
import com.book.manager.dao.UserActionMapper;
import com.book.manager.dao.UserProfileMapper;
import com.book.manager.entity.Book;
import com.book.manager.entity.Borrow;
import com.book.manager.entity.UserAction;
import com.book.manager.entity.UserProfile;
import com.book.manager.entity.Users;
import com.book.manager.util.consts.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserProfileService {

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Autowired
    private UserActionMapper userActionMapper;

    @Autowired
    private BorrowMapper borrowMapper;

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    public UserProfile getOrCompute(Integer userId) {
        if (userId == null) return null;
        UserProfile p = userProfileMapper.findByUserId(userId);
        if (p != null) return p;
        // 没有画像就即时生成一份
        return computeAndSave(userId);
    }

    public UserProfile computeAndSave(Integer userId) {
        Users u = userService.findUserById(userId);
        if (u == null) return null;

        // 1) 活跃度：最近 N 条行为 score 求和
        List<UserAction> actions = userActionMapper.listByUser(userId, 200);
        int activeScore = 0;
        Map<String, Integer> typeCount = new HashMap<>();
        for (UserAction a : actions) {
            if (a == null) continue;
            if (a.getScore() != null) activeScore += a.getScore();
            if (a.getActionType() != null) {
                typeCount.merge(a.getActionType(), 1, Integer::sum);
            }
        }

        // 2) 偏好分类：从 VIEW/借阅过的书取 type 统计 Top3
        Map<String, Integer> catCount = new HashMap<>();
        Set<Integer> bookIds = new HashSet<>();
        for (UserAction a : actions) {
            if (a == null) continue;
            if (a.getBookId() != null) bookIds.add(a.getBookId());
        }
        List<Borrow> borrows = borrowMapper.findAllBorrowByUserId(userId);
        if (borrows != null) {
            for (Borrow b : borrows) {
                if (b != null && b.getBookId() != null) bookIds.add(b.getBookId());
            }
        }
        for (Integer bid : bookIds) {
            try {
                Book book = bookService.findBook(bid);
                String type = book == null ? null : book.getType();
                if (StrUtil.isNotBlank(type)) {
                    // type 可能是 "文化、科学、教育" 这种：按中文逗号/顿号/英文逗号切分
                    String[] parts = type.split("[、,，]\\s*");
                    for (String c : parts) {
                        if (StrUtil.isBlank(c)) continue;
                        catCount.merge(c.trim(), 1, Integer::sum);
                    }
                }
            } catch (Exception ignore) {
            }
        }

        List<Map.Entry<String, Integer>> cats = new ArrayList<>(catCount.entrySet());
        cats.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));
        List<String> topCats = new ArrayList<>();
        for (int i = 0; i < Math.min(3, cats.size()); i++) {
            topCats.add(cats.get(i).getKey());
        }

        // 3) 逾期率：总借阅数中，归还时已逾期的占比（简化）
        int totalBorrow = borrows == null ? 0 : borrows.size();
        int lateCount = 0;
        if (borrows != null) {
            Date now = new Date();
            for (Borrow b : borrows) {
                if (b == null) continue;
                if (b.getEndTime() == null) continue;
                // 已归还的：用 updateTime 近似归还时间；未归还的：用当前时间
                Date retTime = (b.getRet() != null && b.getRet().equals(Constants.YES)) ? b.getUpdateTime() : now;
                if (retTime != null && retTime.after(b.getEndTime())) {
                    lateCount++;
                }
            }
        }
        double overdueRate = totalBorrow == 0 ? 0.0 : (lateCount * 1.0 / totalBorrow);

        UserProfile p = new UserProfile();
        p.setUserId(userId);
        p.setActiveScore(activeScore);
        p.setFavoriteCategories(topCats.isEmpty() ? null : String.join(",", topCats));
        p.setOverdueRate(overdueRate);

        // 保留用户可编辑字段（不被重算覆盖）
        UserProfile old = userProfileMapper.findByUserId(userId);
        if (old != null) {
            p.setGrade(old.getGrade());
            p.setMajors(old.getMajors());
            p.setInterests(old.getInterests());
        }

        userProfileMapper.upsert(p);
        return userProfileMapper.findByUserId(userId);
    }

    public UserProfile updateEditable(Integer userId, String grade, String majors, String interests) {
        if (userId == null) return null;
        UserProfile p = userProfileMapper.findByUserId(userId);
        if (p == null) {
            p = computeAndSave(userId);
        }
        if (p == null) return null;
        p.setGrade(grade);
        p.setMajors(majors);
        p.setInterests(interests);
        userProfileMapper.upsert(p);
        return userProfileMapper.findByUserId(userId);
    }
}
