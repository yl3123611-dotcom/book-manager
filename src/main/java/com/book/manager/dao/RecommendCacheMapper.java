package com.book.manager.dao;

import com.book.manager.entity.RecommendCache;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RecommendCacheMapper {

    RecommendCache findByUserId(@Param("userId") Integer userId);

    int upsert(RecommendCache cache);
}
