package com.book.manager.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import com.book.manager.dao.BookMapper;
import com.book.manager.entity.Book;
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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String response = smartLibrarianService.chat(memoryId, message);
            return R.success(CodeEnum.SUCCESS, response);
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
        if (size == null || size <= 0) size = 5;
        if (size > 10) size = 10;

        try {
            String aiJson = smartLibrarianService.recommend(memoryId, message);
            AiRecommendResult result = parseAiResult(aiJson);

            List<Integer> ids = result.getBookIds() == null ? Collections.emptyList() : result.getBookIds();
            if (ids.isEmpty()) {
                return R.success(CodeEnum.SUCCESS, Collections.emptyList());
            }

            if (ids.size() > size) {
                ids = ids.subList(0, size);
            }

            List<Book> books = bookMapper.selectByIds(ids);
            if (books == null || books.isEmpty()) {
                return R.success(CodeEnum.SUCCESS, Collections.emptyList());
            }

            List<BookOut> outs = new ArrayList<>();
            for (Book b : books) {
                BookOut out = new BookOut();
                BeanUtil.copyProperties(b, out);
                if (b.getPublishTime() != null) {
                    out.setPublishTime(DateUtil.format(b.getPublishTime(), "yyyy-MM-dd"));
                }
                outs.add(out);
            }

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

        // 1) 尝试直接解析 JSON（尽量截取 {...}）
        try {
            int l = text.indexOf('{');
            int rr = text.lastIndexOf('}');
            if (l >= 0 && rr > l) {
                String json = text.substring(l, rr + 1);
                return objectMapper.readValue(json, AiRecommendResult.class);
            }
        } catch (Exception ignore) {
        }

        // 2) JSON 失败：抓 ID:31 / ID：31
        Pattern p = Pattern.compile("(?i)ID\\s*[:：]\\s*(\\d+)");
        Matcher m = p.matcher(text);
        List<Integer> ids = new ArrayList<>();
        while (m.find()) {
            try {
                ids.add(Integer.parseInt(m.group(1)));
            } catch (Exception ignore) {
            }
        }

        r.setBookIds(ids);
        r.setReason("fallback_parse");
        return r;
    }
}
