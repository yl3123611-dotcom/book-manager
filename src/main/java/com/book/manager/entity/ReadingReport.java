package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "阅读报告")
public class ReadingReport {

    private Integer id;

    private Integer userId;

    private Integer booksRead;

    private Integer pagesRead;

    private Integer avgSessionMinutes;

    private java.sql.Date periodStart;

    private java.sql.Date periodEnd;

    private Date createdAt;
}
