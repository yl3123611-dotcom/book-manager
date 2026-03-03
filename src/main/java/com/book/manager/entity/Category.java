package com.book.manager.entity;

import lombok.Data;
import jakarta.persistence.*;

/**
 * @Description 图书类别实体（用于管理）
 */
@Data
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 分类名称
    private String name;

    // 是否启用（1=启用，0=禁用）
    private Integer enabled = 1;

    // 排序权重（小到大）
    private Integer sort = 0;
}

