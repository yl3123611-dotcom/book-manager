package com.book.manager.dao;

import com.book.manager.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper {

    int insert(Notification n);

    int markRead(@Param("id") Integer id, @Param("userId") Integer userId);

    int markAllRead(@Param("userId") Integer userId);

    int archive(@Param("id") Integer id, @Param("userId") Integer userId);

    int unarchive(@Param("id") Integer id, @Param("userId") Integer userId);

    int remove(@Param("id") Integer id, @Param("userId") Integer userId);

    List<Notification> selectByUser(@Param("userId") Integer userId,
                                    @Param("status") Integer status);

    int countUnread(@Param("userId") Integer userId);

    Notification selectById(@Param("id") Integer id, @Param("userId") Integer userId);

    List<Notification> selectArchivedByUser(@Param("userId") Integer userId);

    int sign(@Param("id") Integer id, @Param("userId") Integer userId);

    int reply(@Param("id") Integer id, @Param("userId") Integer userId, @Param("replyContent") String replyContent);

    List<Map<String, Object>> readReceiptByNotificationId(@Param("id") Integer id);
}
