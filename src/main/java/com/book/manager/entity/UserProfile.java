package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户画像")
public class UserProfile {

    private Integer userId;

    @Schema(description = "活跃度分")
    private Integer activeScore;

    @Schema(description = "偏好分类（逗号分隔）")
    private String favoriteCategories;

    private String grade;

    private String interests;

    private String majors;

    @Schema(description = "逾期率")
    private Double overdueRate;

    private Date updatedAt;
}
