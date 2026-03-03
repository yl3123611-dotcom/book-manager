package com.book.manager.modules.ai.service;

import org.springframework.stereotype.Service;

@Service
public class SmartLibrarianFacade {

    private final SmartLibrarianService ai; // 注意：这是 @AiService 生成的代理

    public SmartLibrarianFacade(SmartLibrarianService ai) {
        this.ai = ai;
    }

    public String chat(String memoryId, String userMessage) {
        String mid = normalizeMemoryId(memoryId);
        String msg = normalizeMessage(userMessage);
        return ai.chat(mid, msg);
    }

    public String recommend(String memoryId, String userMessage) {
        String mid = normalizeMemoryId(memoryId);
        String msg = normalizeMessage(userMessage);
        return ai.recommend(mid, msg);
    }

    private String normalizeMemoryId(String memoryId) {
        if (memoryId == null || memoryId.isBlank()) {
            return "default";
        }
        return memoryId.trim();
    }

    private String normalizeMessage(String userMessage) {
        if (userMessage == null) {
            throw new IllegalArgumentException("userMessage cannot be null");
        }
        String msg = userMessage.trim();
        if (msg.isEmpty()) {
            throw new IllegalArgumentException("userMessage cannot be empty");
        }
        return msg;
    }
}

