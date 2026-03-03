package com.book.manager.modules.ai.vo;

import lombok.Data;

import java.util.List;

/**
 * AI 结构化推荐结果（由模型输出 JSON 解析得到）
 */
@Data
public class AiRecommendResult {

    /** 推荐书籍 ID 列表 */
    private List<Integer> bookIds;

    /** 推荐理由（可选） */
    private String reason;
}

