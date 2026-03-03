package com.book.manager.dao;

import com.book.manager.entity.Recommendation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RecommendationMapper {

    int insertBatch(@Param("list") List<Recommendation> list);

    int deleteByUser(@Param("userId") Integer userId);

    List<Recommendation> listByUser(@Param("userId") Integer userId, @Param("limit") Integer limit);
}
