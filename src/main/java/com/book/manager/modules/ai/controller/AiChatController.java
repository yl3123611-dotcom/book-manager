package com.book.manager.modules.ai.controller;

import com.book.manager.modules.ai.service.SmartLibrarianRagStreamingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class AiChatController {

    private final SmartLibrarianRagStreamingService ai;

    public AiChatController(SmartLibrarianRagStreamingService ai) {
        this.ai = ai;
    }

    @GetMapping(value = "/ai/chat/stream", produces = "text/event-stream;charset=utf-8")
    public Flux<String> chatStream(@RequestParam String memoryId,
                                   @RequestParam String message) {
        return ai.chat(memoryId, message);
    }
}

