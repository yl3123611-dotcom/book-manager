package com.book.manager.modules.ai.tools;

import com.book.manager.dao.BookMapper;
import com.book.manager.entity.Book;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
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

    @Tool("根据书籍ID查询单本书的详细信息，包括简介、ISBN、出版社、页数、位置等。")
    public String getBookDetail(int bookId) {
        System.out.println("AI 正在查询书籍详情: bookId=" + bookId);

        List<Book> books = bookMapper.selectByIds(List.of(bookId));
        if (books == null || books.isEmpty()) {
            return "未找到 ID 为 " + bookId + " 的书籍。";
        }
        Book b = books.get(0);
        return String.format(
                "ID:%s\n书名：《%s》\n作者：%s\n分类：%s\n出版社：%s\nISBN：%s\n页数：%s\n定价：%s\n库存：%s\n位置：%s\n简介：%s",
                b.getId(),
                safe(b.getName()),
                safe(b.getAuthor()),
                safe(b.getType()),
                safe(b.getPublish()),
                safe(b.getIsbn()),
                b.getPages() == null ? "未知" : b.getPages().toString(),
                b.getPrice() == null ? "未知" : b.getPrice().toString(),
                b.getSize() == null ? "0" : b.getSize().toString(),
                safe(b.getLocationText()),
                safe(b.getIntroduction())
        );
    }

    @Tool("查询图书馆所有分类及每个分类的图书数量，帮助用户了解馆藏分布。")
    public String listCategories() {
        System.out.println("AI 正在查询馆藏分类统计");

        List<Map<String, Object>> types = bookMapper.countByType();
        if (types == null || types.isEmpty()) {
            return "暂无分类统计信息。";
        }
        StringBuilder sb = new StringBuilder("图书馆馆藏分类统计：\n");
        for (Map<String, Object> row : types) {
            sb.append(String.format("  分类：%s —— %s 本\n",
                    row.getOrDefault("type", "未分类"),
                    row.getOrDefault("cnt", 0)));
        }
        return sb.toString();
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "未知" : s.trim();
    }
}

