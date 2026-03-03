package com.book.manager.dao;

import com.book.manager.entity.HomeBanner;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface HomeBannerMapper {
    List<HomeBanner> listAll();
    List<HomeBanner> listEnabled();
    int insert(HomeBanner banner);
    int update(HomeBanner banner);
    int deleteById(Integer id);
    int setEnabled(Integer id, Integer enabled);
}
