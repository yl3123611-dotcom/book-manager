package com.book.manager.modules.ai.tools;

import com.book.manager.dao.BookMapper;
import com.book.manager.entity.Book;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BookTools {

    @Autowired
    private BookMapper bookMapper;

    @Tool("根据关键词（如书名、作者、ISBN、分类、出版社）在图书馆藏中搜索书籍。返回书籍列表。")
    public String searchBooksInLibrary(String keyword) {
        System.out.println("AI 正在调用数据库搜索书籍: " + keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return "请提供具体的关键词进行搜索。";
        }
        keyword = keyword.trim();

        List<Book> books = bookMapper.selectByKeyword(keyword);

        if (books == null || books.isEmpty()) {
            return "很抱歉，图书馆暂时没有找到包含关键词 '" + keyword + "' 的书籍。";
        }

        // 关键：必须带 ID，模型才能输出 bookIds
        return books.stream()
                .map(b -> String.format(
                        "ID:%s | 书名：《%s》 | 作者：%s | 分类：%s | 出版社：%s | 库存：%s",
                        b.getId(),
                        safe(b.getName()),
                        safe(b.getAuthor()),
                        safe(b.getType()),
                        safe(b.getPublish()),
                        b.getSize() == null ? "0" : b.getSize().toString()
                ))
                .collect(Collectors.joining("\n"));
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "未知" : s.trim();
    }
}

