package com.book.manager.modules.ai.rag;

import com.book.manager.dao.BookMapper;
import com.book.manager.entity.Book;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BookRagIndexer {

    private final BookMapper bookMapper;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;

    // ✅ 默认关闭启动索引，避免 RPM 限制导致启动失败
    @Value("${rag.index-on-startup:false}")
    private boolean indexOnStartup;

    // ✅ 启动时最多索引多少本（默认 3 本，演示够用；认证后可调大）
    @Value("${rag.startup-limit:3}")
    private int startupLimit;

    // ✅ 每本书之间的间隔毫秒数（默认 1200ms，降低触发限流概率）
    @Value("${rag.startup-sleep-ms:1200}")
    private long startupSleepMs;

    public BookRagIndexer(BookMapper bookMapper,
                          EmbeddingModel embeddingModel,
                          EmbeddingStore<TextSegment> embeddingStore) {
        this.bookMapper = bookMapper;
        this.embeddingModel = embeddingModel;
        this.embeddingStore = embeddingStore;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void buildIndex(ApplicationReadyEvent event) {

        if (!indexOnStartup) {
            System.out.println("RAG index-on-startup disabled.");
            return;
        }

        try {
            List<Book> books = bookMapper.findBookListByLike("");
            System.out.println("RAG indexing books: " + books.size());

            int count = 0;
            for (Book b : books) {
                if (count >= startupLimit) break;

                String text = toEmbeddingText(b);

                Metadata md = Metadata.from("bookId", String.valueOf(b.getId()));
                TextSegment segment = TextSegment.from(text, md);

                var embedding = embeddingModel.embed(text).content();
                embeddingStore.add(embedding, segment);

                count++;

                // 降低 RPM 风险
                if (startupSleepMs > 0) {
                    try {
                        Thread.sleep(startupSleepMs);
                    } catch (InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                    }
                }
            }

            System.out.println("RAG indexing done. indexed=" + count + ", totalBooks=" + books.size());
        } catch (Exception e) {
            // ⭐⭐⭐ 关键：不要 throw，不然应用会启动失败 ⭐⭐⭐
            System.err.println("RAG indexing skipped/failed: " + e.getMessage());
        }
    }

    private String toEmbeddingText(Book b) {
        return """
               书名：%s
               作者：%s
               分类：%s
               出版社：%s
               ISBN：%s
               简介：%s
               """
                .formatted(
                        safe(b.getName()),
                        safe(b.getAuthor()),
                        safe(b.getType()),
                        safe(b.getPublish()),
                        safe(b.getIsbn()),
                        safe(b.getIntroduction())
                );
    }

    private String safe(String s) {
        return (s == null || s.isBlank()) ? "未知" : s.trim();
    }
}

