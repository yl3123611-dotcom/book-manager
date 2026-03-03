package com.book.manager.entity;

import lombok.Data;

@Data
public class Seat {
    private Integer id;
    private String seatNo;
    private String room;
    private Integer status; // 0-空闲, 1-使用中
}