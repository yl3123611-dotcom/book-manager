package com.book.manager.entity;

import lombok.Data;

import java.util.Date;

@Data
public class HomeBanner {
    private Integer id;
    private String title;
    private String imageUrl; // /upload/xxx.png
    private String linkUrl;  // 可为空
    private Integer sort;
    private Integer enabled; // 1启用 0禁用
    private Date createTime;
    private Date updateTime;
}
