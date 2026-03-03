package com.book.manager.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Announcement {
    private Integer id;

    private String title;

    private String content;

    /** 1=启用,0=禁用 */
    private Integer enabled;

    /** 1=置顶,0=不置顶 */
    private Integer pinned;

    private LocalDateTime publishTime;

    private Integer createdBy;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
