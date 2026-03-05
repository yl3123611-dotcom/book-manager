package com.book.manager.modules.ai.controller;

import com.book.manager.modules.ai.service.BookKnowledgeService;
import com.book.manager.modules.ai.service.SmartLibrarianRagStreamingService;
import com.book.manager.modules.ai.service.SmartLibrarianService;
import com.book.manager.util.R;
import com.book.manager.util.http.CodeEnum;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class AiChatController {

    private static final Logger log = LoggerFactory.getLogger(AiChatController.class);

    private final SmartLibrarianRagStreamingService ai;

    @Autowired
    private BookKnowledgeService bookKnowledgeService;

    @Autowired
    private ChatMemoryStore chatMemoryStore;

    @Autowired
    private SmartLibrarianService smartLibrarianService;

    public AiChatController(SmartLibrarianRagStreamingService ai) {
        this.ai = ai;
    }

    /**
     * SSE 流式聊天 —— 用 SseEmitter（Spring MVC 原生支持）桥接 LangChain4j 的 Flux
     * <p>
     * 原因：本项目是 spring-boot-starter-web（Servlet 栈），不是 WebFlux 栈。
     * 直接返回 Flux&lt;String&gt; 会导致 500 错误，因为 Spring MVC 不知道怎么处理 Flux。
     * 需要手动订阅 Flux，把每个 token 通过 SseEmitter.send() 推送给浏览器。
     */
    @GetMapping(value = "/ai/chat/stream", produces = "text/event-stream;charset=utf-8")
    public SseEmitter chatStream(@RequestParam String memoryId,
                                 @RequestParam String message) {

        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);

        String safeMemoryId = (memoryId == null || memoryId.isBlank()) ? "default" : memoryId.trim();
        String safeMessage = message == null ? "" : message.trim();
        if (safeMessage.isEmpty()) {
            try {
                emitter.send(SseEmitter.event().name("error").data("消息不能为空"));
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        String enrichedMessage;
        try {
            enrichedMessage = bookKnowledgeService.buildPromptWithContext(safeMessage);
        } catch (Exception e) {
            log.error("[AI stream] knowledge build failed, memoryId={}", safeMemoryId, e);
            try {
                emitter.send(SseEmitter.event().name("error").data("知识库加载失败，请稍后重试"));
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        Flux<String> flux;
        try {
            flux = ai.chat(safeMemoryId, enrichedMessage);
        } catch (Exception e) {
            log.error("[AI stream] init failed, memoryId={}, messageLength={}", safeMemoryId, safeMessage.length(), e);

            // streaming 初始化失败时自动降级到非流式，尽量保证用户可用性
            try {
                String reply = smartLibrarianService.chat(safeMemoryId, enrichedMessage);
                emitter.send(SseEmitter.event().data(reply == null ? "" : reply));
                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
            } catch (Exception fallbackEx) {
                log.error("[AI stream] sync fallback failed, memoryId={}", safeMemoryId, fallbackEx);
                try {
                    emitter.send(SseEmitter.event().name("error").data("AI 服务初始化失败，请重试"));
                } catch (IOException ignored) {
                }
            }
            emitter.complete();
            return emitter;
        }

        Disposable subscription;
        try {
            subscription = flux.subscribe(
                    token -> {
                        try {
                            emitter.send(SseEmitter.event().data(token));
                        } catch (Exception e) {
                            // 客户端断开或通道已关闭时，正常结束即可，避免抛出到容器层
                            emitter.complete();
                        }
                    },
                    error -> {
                        log.error("[AI stream] runtime error, memoryId={}", safeMemoryId, error);
                        try {
                            emitter.send(SseEmitter.event().name("error").data("AI 调用失败，请稍后重试"));
                        } catch (IOException ignored) {
                        }
                        emitter.complete();
                    },
                    () -> {
                        try {
                            emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                        } catch (IOException ignored) {
                        }
                        emitter.complete();
                    }
            );
        } catch (Exception e) {
            log.error("[AI stream] subscribe failed, memoryId={}", safeMemoryId, e);
            try {
                emitter.send(SseEmitter.event().name("error").data("AI 服务异常，请稍后重试"));
            } catch (IOException ignored) {
            }
            emitter.complete();
            return emitter;
        }

        // 客户端断开时取消订阅，避免浪费 API 调用
        emitter.onCompletion(subscription::dispose);
        emitter.onTimeout(() -> {
            subscription.dispose();
            emitter.complete();
        });
        emitter.onError(t -> subscription.dispose());

        return emitter;
    }

    /**
     * 清除指定会话的聊天记忆（Redis）
     */
    @DeleteMapping("/ai/chat/memory")
    public R clearMemory(@RequestParam String memoryId) {
        if (memoryId == null || memoryId.isBlank()) {
            return R.failMsg("memoryId 不能为空");
        }
        try {
            chatMemoryStore.deleteMessages(memoryId);
            return R.success(CodeEnum.SUCCESS, "会话记忆已清除");
        } catch (Exception e) {
            return R.failMsg("清除失败：" + e.getMessage());
        }
    }

    /**
     * AI 模块健康检查
     */
    @GetMapping("/ai/status")
    public R aiStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        status.put("knowledgeDocs", bookKnowledgeService.getDocCount());
        status.put("streamingServiceAvailable", ai != null);
        status.put("chatMemoryStoreAvailable", chatMemoryStore != null);
        status.put("syncServiceAvailable", smartLibrarianService != null);
        return R.success(CodeEnum.SUCCESS, status);
    }
}
