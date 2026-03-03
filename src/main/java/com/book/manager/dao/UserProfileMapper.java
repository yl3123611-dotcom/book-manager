package com.book.manager.dao;

import com.book.manager.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserProfileMapper {

    UserProfile findByUserId(@Param("userId") Integer userId);

    int upsert(UserProfile profile);
}
