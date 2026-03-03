package com.book.manager.dao;

import com.book.manager.entity.UserAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserActionMapper {

    int insert(UserAction action);

    List<UserAction> listByUser(@Param("userId") Integer userId,
                               @Param("limit") Integer limit);

    List<UserAction> listByBook(@Param("bookId") Integer bookId,
                               @Param("limit") Integer limit);
}
