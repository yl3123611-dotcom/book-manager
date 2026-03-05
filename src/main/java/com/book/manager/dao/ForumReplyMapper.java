package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ForumReplyMapper {

    int insertReply(@Param("postId") Long postId,
                    @Param("userId") Integer userId,
                    @Param("content") String content,
                    @Param("parentId") Long parentId,
                    @Param("rootId") Long rootId);

    List<Map<String, Object>> listByPost(@Param("postId") Long postId);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    Map<String,Object> selectById(@Param("id") Long id);

    int countVisibleByPost(@Param("postId") Long postId);

    int existsReplyInPost(@Param("postId") Long postId, @Param("replyId") Long replyId);
}
