package com.book.manager.dao;

import com.book.manager.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper
@Component
public interface AnnouncementMapper {

    List<Announcement> selectTop(@Param("limit") int limit);

    List<Announcement> listAll();

    List<Announcement> listPage(@Param("offset") int offset, @Param("size") int size);

    int countAll();

    Announcement selectById(@Param("id") Integer id);

    int insert(Announcement a);

    int update(Announcement a);

    int deleteById(@Param("id") Integer id);

    int setEnabled(@Param("id") Integer id, @Param("enabled") Integer enabled);

    int setPinned(@Param("id") Integer id, @Param("pinned") Integer pinned);
}
