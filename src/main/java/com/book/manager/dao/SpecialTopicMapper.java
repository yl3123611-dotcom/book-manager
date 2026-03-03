package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SpecialTopicMapper {

    List<Map<String,Object>> listTopics();

    Map<String,Object> findBySlug(@Param("slug") String slug);

    List<Map<String,Object>> listTopicBooks(@Param("topicId") Integer topicId);

    int insertTopic(@Param("slug") String slug, @Param("name") String name, @Param("description") String description);

    int addTopicBook(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId, @Param("sort") Integer sort);

    List<Map<String,Object>> listTopicsByBookId(@Param("bookId") Integer bookId);

    int removeTopicBook(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId);
}
