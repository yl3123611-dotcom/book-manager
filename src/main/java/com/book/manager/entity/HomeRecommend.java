package com.book.manager.entity;

import lombok.Data;

import java.util.Date;

@Data
public class HomeRecommend {
    private Integer id;
    private Integer bookId;
    private Integer sort;
    private Integer enabled; // 1启用 0禁用
    private Date createTime;
}

