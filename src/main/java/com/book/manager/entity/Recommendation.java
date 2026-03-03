package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "推荐明细")
public class Recommendation {
    private Integer id;
    private Integer userId;
    private Integer bookId;
    private Double score;
    private String source;
    private Date computedAt;
}
