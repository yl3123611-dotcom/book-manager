package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumPostMapper {

    int insertPost(@Param("userId") Integer userId,
                   @Param("title") String title,
                   @Param("content") String content,
                   @Param("category") String category);

    List<Map<String, Object>> listPosts(@Param("keyword") String keyword,
                                        @Param("category") String category);

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

    int addFavorite(@Param("postId") Long postId, @Param("userId") Integer userId);

    int removeFavorite(@Param("postId") Long postId, @Param("userId") Integer userId);

    int existsFavorite(@Param("postId") Long postId, @Param("userId") Integer userId);

    int updateAcceptedReply(@Param("postId") Long postId, @Param("replyId") Long replyId, @Param("userId") Integer userId);

    int updateMyPost(@Param("id") Long id,
                     @Param("userId") Integer userId,
                     @Param("title") String title,
                     @Param("content") String content,
                     @Param("category") String category);

    int deleteMyPost(@Param("id") Long id, @Param("userId") Integer userId);

    int insertReport(@Param("postId") Long postId,
                     @Param("userId") Integer userId,
                     @Param("reason") String reason);

    List<Map<String, Object>> listReports(@Param("status") Integer status,
                                          @Param("keyword") String keyword);

    int handleReport(@Param("id") Long id,
                     @Param("status") Integer status,
                     @Param("handleNote") String handleNote,
                     @Param("handledBy") Integer handledBy);

    List<Map<String, Object>> listMyPosts(@Param("userId") Integer userId, @Param("keyword") String keyword);

    List<Map<String, Object>> listMyFavorites(@Param("userId") Integer userId, @Param("keyword") String keyword);
}
