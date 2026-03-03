package com.book.manager.entity;

import lombok.Data;

@Data
public class ReadingRoom {
    private Integer id;
    private String name;      // 阅览室名称
    private String location;  // 位置
    private Integer enabled;  // 是否启用 1-启用 0-禁用
    private Integer size;     // 容量
}
