package com.book.manager.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper {

    List<Map<String,Object>> listEnabled();

    List<Map<String,Object>> adminList();

    int insert(@Param("name") String name, @Param("sort") Integer sort);

    int update(@Param("id") Integer id, @Param("name") String name, @Param("sort") Integer sort, @Param("enabled") Integer enabled);

    int delete(@Param("id") Integer id);

    Map<String, Object> findByName(@Param("name") String name);

    int insertIgnore(@Param("name") String name, @Param("sort") Integer sort);
}
