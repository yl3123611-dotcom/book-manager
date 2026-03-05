package com.book.manager.util.vo;

import lombok.Data;

import java.util.Date;

@Data
public class RecommendationOut {
    private Integer bookId;
    private Double score;
    private String source;
    private Date computedAt;

    private String bookName;
    private String author;
    private String type;
    private String cover;
    private String introduction;
}
