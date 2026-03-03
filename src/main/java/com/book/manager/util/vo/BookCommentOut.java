package com.book.manager.util.vo;

import lombok.Data;

@Data
public class BookCommentOut {
    private Long id;
    private Integer bookId;
    private Integer userId;
    private String nickname;
    private String username;
    private String content;
    private String createdAt;
}
