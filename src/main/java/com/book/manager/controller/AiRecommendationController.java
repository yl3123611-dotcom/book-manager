package com.book.manager.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BookMapper;
import com.book.manager.entity.Book;
import com.book.manager.modules.ai.service.BookKnowledgeService;
import com.book.manager.modules.ai.service.SmartLibrarianService;
import com.book.manager.modules.ai.vo.AiRecommendResult;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import com.book.manager.util.vo.BookOut;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Tag(name = "AI智能推荐")
@RestController
@RequestMapping("/ai")
public class AiRecommendationController {

    @Autowired
    private SmartLibrarianService smartLibrarianService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private BookKnowledgeService bookKnowledgeService;

    @Operation(summary = "智能对话/推荐（返回自然语言文本）")
    @PostMapping("/chat")
    public R chat(HttpServletRequest request,
                  @RequestParam(defaultValue = "default") String memoryId) {

        // 1) 兼容 jQuery 默认 application/x-www-form-urlencoded / query 参数
        String message = request.getParameter("message");

        // 2) 兼容 JSON body: {"message":"xxx"}
        if (message == null || message.isBlank()) {
            try {
                String contentType = request.getContentType();
                if (contentType != null && contentType.contains("application/json")) {
                    String body = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
                    if (body != null && !body.isBlank()) {
                        Map<String, Object> map = objectMapper.readValue(body, Map.class);
                        Object m = map.get("message");
                        if (m != null) message = String.valueOf(m);
                    }
                }
            } catch (Exception ignored) {
            }
        }

        if (message == null || message.isBlank()) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }

        try {
            String prompt = bookKnowledgeService.buildPromptWithContext(message);
            String response = smartLibrarianService.chat(memoryId, prompt);
            return R.success(CodeEnum.SUCCESS, response);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 判断用户消息是否是推荐意图（含有推荐/想看/想学/查找/搜索/有没有/哪些 等关键词）
     * 非推荐意图（如"你好"、"谢谢"、"再见"等）应走 chat 而非 recommend。
     */
    private boolean isRecommendIntent(String msg) {
        if (msg == null) return false;
        String lower = msg.trim().toLowerCase();
        // 推荐意图关键词
        String[] recKeywords = {
                "推荐", "想看", "想读", "想学", "想找", "有没有", "有什么",
                "查找", "搜索", "书", "哪些", "类似", "适合", "经典",
                "入门", "进阶", "排行", "书单", "分类", "馆藏",
                "历史", "科幻", "文学", "小说", "编程", "心理",
                "党史", "科普", "教育", "哲学", "经济", "管理",
                "介绍", "详情", "简介"
        };
        for (String kw : recKeywords) {
            if (lower.contains(kw)) return true;
        }
        return false;
    }

    @Operation(summary = "增强版AI推荐（返回推荐理由+图书列表+来源）")
    @PostMapping("/recommend-plus")
    public R recommendPlus(@RequestParam String message,
                           @RequestParam(defaultValue = "default") String memoryId,
                           @RequestParam(defaultValue = "5") Integer size) {

        if (message == null || message.trim().isEmpty()) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        int safeSize = normalizeSize(size);
        String query = message.trim();

        // ★ 非推荐意图（如"你好"、"谢谢"）直接走 chat 对话，避免强行推荐
        if (!isRecommendIntent(query)) {
            try {
                String prompt = bookKnowledgeService.buildPromptWithContext(query);
                String chatReply = smartLibrarianService.chat(memoryId, prompt);
                Map<String, Object> payload = new HashMap<>();
                payload.put("query", query);
                payload.put("books", Collections.emptyList());
                payload.put("source", "chat");
                payload.put("reply", chatReply != null ? chatReply.trim() : "您好，有什么可以帮您的吗？");
                return R.success(CodeEnum.SUCCESS, payload);
            } catch (Exception e) {
                e.printStackTrace();
                return R.fail(CodeEnum.SYSTEM_ERROR);
            }
        }

        try {
            String prompt = bookKnowledgeService.buildPromptWithContext(query);
            String aiJson = smartLibrarianService.recommend(memoryId, prompt);
            AiRecommendResult result = parseAiResult(aiJson);
            List<Integer> aiIds = limitIds(result.getBookIds(), safeSize);

            List<BookOut> books = toBookOuts(aiIds, safeSize);
            Map<String, Object> payload = new HashMap<>();
            payload.put("query", query);

            if (!books.isEmpty()) {
                payload.put("books", books);
                payload.put("source", "ai");
                payload.put("reply", safeReason(result.getReason(), "已根据馆藏为你生成推荐。"));
                return R.success(CodeEnum.SUCCESS, payload);
            }

            // ★ AI 返回空 bookIds 但有 reason（如"本馆暂未收录"），直接返回 reason 作为回复
            String aiReason = result.getReason();
            if (aiReason != null && !aiReason.isEmpty()
                    && !"fallback_parse".equals(aiReason) && !"empty".equals(aiReason)) {
                payload.put("books", Collections.emptyList());
                payload.put("source", "ai");
                payload.put("reply", aiReason);
                return R.success(CodeEnum.SUCCESS, payload);
            }

            // AI 无可用 ID 时，降级为数据库关键词推荐，保证用户有结果可看。
            List<Book> fallbackBooks = bookMapper.selectByKeyword(query);
            List<Integer> fallbackIds = fallbackBooks == null ? Collections.emptyList() :
                    fallbackBooks.stream().map(Book::getId).filter(id -> id != null && id > 0)
                            .limit(safeSize).collect(Collectors.toList());
            List<BookOut> fallbackOuts = toBookOuts(fallbackIds, safeSize);

            payload.put("books", fallbackOuts);
            payload.put("source", "fallback");
            payload.put("reply", fallbackOuts.isEmpty()
                    ? "本馆暂未检索到匹配书籍，你可以换一个关键词试试。"
                    : "已按关键词为你匹配了馆藏图书。"
            );
            return R.success(CodeEnum.SUCCESS, payload);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 结构化推荐：AI 只输出 bookIds（JSON），后端再按 ID 查库，返回 BookOut 列表给前端。
     */
    @Operation(summary = "AI结构化推荐（返回 BookOut 列表）")
    @PostMapping("/recommend")
    public R recommend(@RequestParam String message,
                       @RequestParam(defaultValue = "default") String memoryId,
                       @RequestParam(defaultValue = "5") Integer size) {

        if (message == null || message.trim().isEmpty()) {
            return R.fail(CodeEnum.PARAM_ERROR);
        }
        int safeSize = normalizeSize(size);

        try {
            String prompt = bookKnowledgeService.buildPromptWithContext(message.trim());
            String aiJson = smartLibrarianService.recommend(memoryId, prompt);
            AiRecommendResult result = parseAiResult(aiJson);
            List<Integer> ids = limitIds(result.getBookIds(), safeSize);
            List<BookOut> outs = toBookOuts(ids, safeSize);
            return R.success(CodeEnum.SUCCESS, outs);
        } catch (Exception e) {
            e.printStackTrace();
            return R.fail(CodeEnum.SYSTEM_ERROR);
        }
    }

    /**
     * 解析模型输出：优先按 JSON 解析；失败则尝试从文本里提取 ID（兜底）。
     */
    private AiRecommendResult parseAiResult(String aiOutput) {
        AiRecommendResult r = new AiRecommendResult();
        if (aiOutput == null) {
            r.setBookIds(Collections.emptyList());
            r.setReason("empty");
            return r;
        }
        String text = aiOutput.trim();
        System.out.println("[AI recommend] raw output: " + (text.length() > 500 ? text.substring(0, 500) + "..." : text));

        // 1) 尝试直接解析 JSON（尽量截取 {...}）
        try {
            int l = text.indexOf('{');
            int rr = text.lastIndexOf('}');
            if (l >= 0 && rr > l) {
                String json = text.substring(l, rr + 1);
                AiRecommendResult parsed = objectMapper.readValue(json, AiRecommendResult.class);
                parsed.setBookIds(limitIds(parsed.getBookIds(), 10));
                // 确保 reason 不为 null
                if (parsed.getReason() == null || parsed.getReason().isBlank()) {
                    parsed.setReason("已根据馆藏为你生成推荐。");
                }
                return parsed;
            }
        } catch (Exception e) {
            System.out.println("[AI recommend] JSON parse failed: " + e.getMessage());
        }

        // 2) JSON 失败：抓 ID:31 / ID：31（工具输出中常见格式）
        Pattern idPattern = Pattern.compile("(?i)ID\\s*[:：]\\s*(\\d+)");
        Matcher idMatcher = idPattern.matcher(text);
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        while (idMatcher.find()) {
            try {
                ids.add(Integer.parseInt(idMatcher.group(1)));
            } catch (Exception ignore) {
            }
        }

        // 3) 兜底：抓类似 bookIds:[1,2,3] 的纯数字
        if (ids.isEmpty()) {
            Pattern arrPattern = Pattern.compile("bookIds\\s*[:：]\\s*\\[(.*?)\\]", Pattern.CASE_INSENSITIVE);
            Matcher arrMatcher = arrPattern.matcher(text);
            if (arrMatcher.find()) {
                Matcher num = Pattern.compile("\\d+").matcher(arrMatcher.group(1));
                while (num.find()) {
                    try {
                        ids.add(Integer.parseInt(num.group()));
                    } catch (Exception ignore) {
                    }
                }
            }
        }

        r.setBookIds(new ArrayList<>(ids));
        // ★ 不要用内部标记作为用户可见的 reason
        r.setReason(ids.isEmpty() ? "" : "已根据馆藏为你整理了推荐。");
        return r;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size <= 0) return 5;
        return Math.min(size, 10);
    }

    private String safeReason(String reason, String defaultValue) {
        if (reason == null || reason.trim().isEmpty()) {
            return defaultValue;
        }
        return reason.trim();
    }

    private List<Integer> limitIds(List<Integer> ids, int maxSize) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        return ids.stream()
                .filter(id -> id != null && id > 0)
                .distinct()
                .limit(maxSize)
                .collect(Collectors.toList());
    }

    private List<BookOut> toBookOuts(List<Integer> ids, int sizeLimit) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<Book> books = bookMapper.selectByIds(ids);
        if (books == null || books.isEmpty()) {
            return Collections.emptyList();
        }

        // selectByIds 不保证顺序，这里按推荐 ID 顺序重排。
        LinkedHashMap<Integer, Book> byId = new LinkedHashMap<>();
        for (Book b : books) {
            if (b != null && b.getId() != null) {
                byId.put(b.getId(), b);
            }
        }

        List<BookOut> outs = new ArrayList<>();
        for (Integer id : ids) {
            Book b = byId.get(id);
            if (b == null) continue;
            if (b.getSize() != null && b.getSize() <= 0) continue;
            BookOut out = new BookOut();
            BeanUtil.copyProperties(b, out);
            if (b.getPublishTime() != null) {
                out.setPublishTime(DateUtil.format(b.getPublishTime(), "yyyy-MM-dd"));
            }
            outs.add(out);
            if (outs.size() >= sizeLimit) break;
        }
        return outs;
    }
}
