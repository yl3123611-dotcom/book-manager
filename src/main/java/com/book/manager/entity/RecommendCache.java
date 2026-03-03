package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "推荐缓存")
public class RecommendCache {
    private Integer userId;
    private String recsJson;
    private Date updatedAt;
}
