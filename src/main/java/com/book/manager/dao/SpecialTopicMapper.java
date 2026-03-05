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

    int addTopicBookFavorite(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId, @Param("userId") Integer userId);

    int removeTopicBookFavorite(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId, @Param("userId") Integer userId);

    int existsTopicBookFavorite(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId, @Param("userId") Integer userId);

    int addShareLog(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId, @Param("userId") Integer userId,
                    @Param("shareChannel") String shareChannel, @Param("shareContent") String shareContent);

    List<Map<String, Object>> relatedTopics(@Param("topicId") Integer topicId, @Param("bookId") Integer bookId);

    List<Map<String, Object>> topicStats(@Param("topicId") Integer topicId);
}
