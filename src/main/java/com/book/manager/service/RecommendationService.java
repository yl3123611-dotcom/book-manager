package com.book.manager.service;

import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.RecommendCacheMapper;
import com.book.manager.dao.RecommendationMapper;
import com.book.manager.entity.RecommendCache;
import com.book.manager.entity.Recommendation;
import com.book.manager.entity.Users;
import com.book.manager.modules.ai.service.BookKnowledgeService;
import com.book.manager.modules.ai.service.SmartLibrarianService;
import com.book.manager.util.vo.BookOut;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RecommendationService {

    @Autowired
    private RecommendCacheMapper recommendCacheMapper;

    @Autowired
    private RecommendationMapper recommendationMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private BookService bookService;

    @Autowired
    private SmartLibrarianService smartLibrarianService;

    @Autowired
    private BookKnowledgeService bookKnowledgeService;

    @Autowired
    private ObjectMapper objectMapper;

    public List<BookOut> getCachedRecs(Integer userId) {
        RecommendCache cache = recommendCacheMapper.findByUserId(userId);
        if (cache == null || cache.getRecsJson() == null || cache.getRecsJson().isBlank()) {
            return Collections.emptyList();
        }
        try {
            List<Integer> ids = objectMapper.readValue(cache.getRecsJson(), new TypeReference<List<Integer>>() {});
            if (ids == null || ids.isEmpty()) return Collections.emptyList();
            List<BookOut> out = new ArrayList<>();
            for (Integer id : ids) {
                if (id == null) continue;
                BookOut b = bookService.findBookById(id);
                if (b != null) out.add(b);
            }
            return out;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional
    public List<BookOut> refresh(Integer userId, Integer size) {
        if (size == null || size <= 0) size = 5;
        if (size > 10) size = 10;

        Users u = userService.findUserById(userId);
        if (u == null) return Collections.emptyList();

        // 描述画像作为 prompt
        var profile = userProfileService.getOrCompute(userId);
        String prompt = "请基于用户画像推荐" + size + "本馆藏图书，仅返回JSON：{\"bookIds\":[1,2],\"reason\":\"...\"}。" +
                "用户画像：activeScore=" + (profile == null ? 0 : profile.getActiveScore()) +
                ", favoriteCategories=" + (profile == null ? "" : String.valueOf(profile.getFavoriteCategories())) +
                ", overdueRate=" + (profile == null ? 0 : profile.getOverdueRate());

        String knowledgePrompt = bookKnowledgeService.buildPromptWithContext(prompt);
        String aiJson = smartLibrarianService.recommend("rec-" + userId, knowledgePrompt);

        // 复用 AiRecommendationController 的解析逻辑比较麻烦，这里做最简单 JSON 解析
        List<Integer> ids = new ArrayList<>();
        try {
            int l = aiJson.indexOf('{');
            int r = aiJson.lastIndexOf('}');
            String json = (l >= 0 && r > l) ? aiJson.substring(l, r + 1) : aiJson;
            Map<String, Object> map = objectMapper.readValue(json, Map.class);
            Object o = map.get("bookIds");
            if (o instanceof List<?> list) {
                for (Object x : list) {
                    try { ids.add(Integer.parseInt(String.valueOf(x))); } catch (Exception ignore) {}
                }
            }
        } catch (Exception ignore) {
        }

        if (ids.size() > size) ids = ids.subList(0, size);

        // 写 recommendation 明细
        recommendationMapper.deleteByUser(userId);
        List<Recommendation> recs = new ArrayList<>();
        double base = 1.0;
        for (int i = 0; i < ids.size(); i++) {
            Integer bid = ids.get(i);
            if (bid == null) continue;
            Recommendation rec = new Recommendation();
            rec.setUserId(userId);
            rec.setBookId(bid);
            rec.setScore(base - i * 0.01);
            rec.setSource("AI");
            rec.setComputedAt(DateUtil.date());
            recs.add(rec);
        }
        if (!recs.isEmpty()) {
            recommendationMapper.insertBatch(recs);
        }

        // 写缓存
        RecommendCache cache = new RecommendCache();
        cache.setUserId(userId);
        try {
            cache.setRecsJson(objectMapper.writeValueAsString(ids));
        } catch (Exception e) {
            cache.setRecsJson("[]");
        }
        recommendCacheMapper.upsert(cache);

        // 输出 BookOut
        List<BookOut> out = new ArrayList<>();
        for (Integer id : ids) {
            BookOut b = bookService.findBookById(id);
            if (b != null) out.add(b);
        }
        return out;
    }

    public java.util.List<com.book.manager.util.vo.RecommendationOut> history(Integer userId, Integer limit) {
        if (userId == null) return java.util.Collections.emptyList();
        if (limit == null || limit <= 0) limit = 50;
        if (limit > 200) limit = 200;

        java.util.List<com.book.manager.entity.Recommendation> list = recommendationMapper.listByUser(userId, limit);
        if (list == null || list.isEmpty()) return java.util.Collections.emptyList();

        java.util.List<com.book.manager.util.vo.RecommendationOut> outs = new java.util.ArrayList<>();
        for (com.book.manager.entity.Recommendation r : list) {
            if (r == null) continue;
            com.book.manager.util.vo.RecommendationOut out = new com.book.manager.util.vo.RecommendationOut();
            out.setBookId(r.getBookId());
            out.setScore(r.getScore());
            out.setSource(r.getSource());
            out.setComputedAt(r.getComputedAt());
            if (r.getBookId() != null) {
                com.book.manager.entity.Book b = bookService.findBook(r.getBookId());
                if (b != null) {
                    out.setBookName(b.getName());
                    out.setAuthor(b.getAuthor());
                    out.setType(b.getType());
                    out.setCover(b.getCover());
                    out.setIntroduction(b.getIntroduction());
                }
            }
            outs.add(out);
        }
        return outs;
    }
}
