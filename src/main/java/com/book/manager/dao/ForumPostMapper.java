package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostMapper {

    int insertPost(@Param("userId") Integer userId,
                   @Param("title") String title,
                   @Param("content") String content);

    List<Map<String, Object>> listPosts(@Param("keyword") String keyword);

    Map<String, Object> selectById(@Param("id") Long id);

    int incViewCount(@Param("id") Long id);

    // increment reply count
    int incReplyCount(@Param("id") Long id);

    int decReplyCount(@Param("id") Long id);

    int updateReplyCount(@Param("id") Long id, @Param("replyCount") Integer replyCount);

    // ---- admin methods ----
    /** 列表（包含已删除/隐藏，用于管理） */
    List<Map<String, Object>> adminList(@Param("keyword") String keyword);

    /** 更新状态（0 正常，1 隐藏，2 删除） */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /** 置顶/取消置顶 */
    int updatePinned(@Param("id") Long id, @Param("isPinned") Integer isPinned);

    /** 加精/取消加精 */
    int updateFeatured(@Param("id") Long id, @Param("isFeatured") Integer isFeatured);
}
