package com.book.manager.modules.ai.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = "bookTools"
)
public interface SmartLibrarianRagStreamingService {

    @SystemMessage("""
        你是一位热情、专业的“图书馆智能推荐顾问”。
        你必须优先使用检索到的馆藏信息来推荐书籍，不要编造不存在的书。
        当用户想要推荐/相似书/按主题找书时：
        - 先基于检索到的内容给出候选书
        - 如需更精确的信息（如库存/馆藏位置/更完整字段），再调用工具查询数据库
        回答要亲切，可使用 emoji。
        """)
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}

