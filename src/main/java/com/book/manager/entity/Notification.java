package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "站内通知")
public class Notification {

    @Schema(description = "主键")
    private Integer id;

    @Schema(description = "通知标题")
    private String title;

    @Schema(description = "通知内容")
    private String content;

    @Schema(description = "通知类型，如 DUE_REMINDER / OVERDUE_ALERT")
    private String type;

    @Schema(description = "通知状态：0未读 1已读")
    private Integer status;

    @Schema(description = "重试次数")
    private Integer attempts;

    @Schema(description = "关联借阅记录")
    private Integer borrowId;

    @Schema(description = "接收用户")
    private Integer userId;

    @Schema(description = "创建时间")
    private Date createdAt;

    @Schema(description = "更新时间")
    private Date updatedAt;
}
