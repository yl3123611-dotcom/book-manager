package com.book.manager.dao;

import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface HotSearchMapper {

    @Insert("INSERT INTO hot_search(keyword, cnt, updated_at) VALUES(#{keyword}, 1, NOW()) ON DUPLICATE KEY UPDATE cnt = cnt + 1, updated_at = NOW()")
    int upsertIncrement(@Param("keyword") String keyword);

    @Select("SELECT keyword FROM hot_search ORDER BY cnt DESC, updated_at DESC LIMIT #{size}")
    List<String> listTop(@Param("size") int size);

    @Select("SELECT keyword, cnt FROM hot_search ORDER BY cnt DESC, updated_at DESC LIMIT #{size}")
    List<java.util.Map<String, Object>> listTopWithCount(@Param("size") int size);
}
