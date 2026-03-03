package com.book.manager.dao;

import com.book.manager.util.vo.BookCommentOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BookCommentMapper {

    int insert(@Param("bookId") Integer bookId,
               @Param("userId") Integer userId,
               @Param("content") String content);

    List<BookCommentOut> listByBook(@Param("bookId") Integer bookId);

    int hide(@Param("id") Long id);

    int delete(@Param("id") Long id);
}
