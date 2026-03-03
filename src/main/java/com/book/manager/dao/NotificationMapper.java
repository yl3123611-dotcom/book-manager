package com.book.manager.dao;

import com.book.manager.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {

    int insert(Notification n);

    int markRead(@Param("id") Integer id, @Param("userId") Integer userId);

    int markAllRead(@Param("userId") Integer userId);

    List<Notification> selectByUser(@Param("userId") Integer userId,
                                    @Param("status") Integer status);

    int countUnread(@Param("userId") Integer userId);
}
