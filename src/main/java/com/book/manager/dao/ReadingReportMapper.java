package com.book.manager.dao;

import com.book.manager.entity.ReadingReport;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReadingReportMapper {

    ReadingReport findByUserAndPeriod(@Param("userId") Integer userId,
                                      @Param("periodStart") java.sql.Date periodStart,
                                      @Param("periodEnd") java.sql.Date periodEnd);

    int insert(ReadingReport report);

    int update(ReadingReport report);
}
