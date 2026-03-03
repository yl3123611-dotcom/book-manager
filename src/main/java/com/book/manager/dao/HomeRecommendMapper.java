package com.book.manager.dao;

import com.book.manager.entity.HomeRecommend;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HomeRecommendMapper {
    List<HomeRecommend> listAll();
    List<HomeRecommend> listEnabled();
    int insert(HomeRecommend rec);
    int deleteByBookId(Integer bookId);
    int setEnabled(Integer id, Integer enabled);
}
