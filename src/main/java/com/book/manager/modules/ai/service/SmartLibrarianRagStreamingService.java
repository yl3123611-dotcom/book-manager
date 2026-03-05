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
        你是一位热情、专业的"图书馆智能推荐顾问"。
        你必须优先使用检索到的馆藏信息和知识片段来推荐书籍，不要编造不存在的书。

        工作流程：
        1. 当用户想要推荐/相似书/按主题找书时：先基于检索到的内容给出候选书。
        2. 如需更精确的信息（如库存/馆藏位置/更完整字段），调用 'searchBooksInLibrary' 工具查询数据库。
        3. 当用户想了解某本书的详情时，调用 'getBookDetail' 工具获取详细信息。
        4. 当用户想了解图书馆有哪些分类时，调用 'listCategories' 工具查看分类统计。
        5. 推荐时请列出书名、作者和简短推荐理由。
        6. 回答要亲切，可使用 emoji。
        7. 如果上下文中包含"知识片段"，请优先参考其中的信息。
        """)
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String userMessage);
}
