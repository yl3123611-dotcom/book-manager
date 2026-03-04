package com.book.manager.dao;

import com.book.manager.entity.Book;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Description 图书 Mapper
 */
@Mapper
@Component
public interface BookMapper {

    /**
     * 模糊查询图书列表（支持：书名/ISBN/作者/分类/出版社）
     * @param keyword 关键字
     * @return List<Book>
     */
    List<Book> findBookListByLike(String keyword);

    /**
     * 编辑图书
     * @param map 更新字段 Map（必须包含 id）
     * @return 影响行数
     */
    int updateBook(Map<String, Object> map);

    /**
     * ✅ 给 AI 工具使用的“搜索图书”方法
     * 建议直接复用模糊查询逻辑，用 String keyword 统一入口
     */
    List<Book> selectByBook(String keyword);
    // 添加接口定义
    List<Book> selectByKeyword(@Param("keyword") String keyword);
    List<Book> selectByIds(@Param("ids") List<Integer> ids);

    List<Map<String, Object>> countByType();
}
