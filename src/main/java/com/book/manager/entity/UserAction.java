package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

@Data
@Schema(description = "用户行为记录")
public class UserAction {

    private Long id;

    @Schema(description = "行为类型：VIEW/BORROW/RETURN/RENEW/LIKE 等")
    private String actionType;

    @Schema(description = "图书ID")
    private Integer bookId;

    @Schema(description = "行为发生时间")
    private Date createdAt;

    @Schema(description = "行为分值")
    private Integer score;

    @Schema(description = "用户ID")
    private Integer userId;
}
