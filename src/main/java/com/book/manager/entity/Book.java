package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

/**
 * @Description 图书实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Entity
@Table(name = "book")
@Schema(description = "图书实体")
public class Book {

    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Schema(description = "图书 ISBN 编码")
    private String isbn;

    @Schema(description = "图书名称")
    private String name;

    @Schema(description = "图书作者")
    private String author;

    @Schema(description = "图书页数")
    private Integer pages;

    @Schema(description = "翻译")
    private String translate;

    @Schema(description = "出版社")
    private String publish;

    @Schema(description = "单价")
    private Double price;

    @Schema(description = "库存")
    private Integer size;

    @Schema(description = "分类")
    private String type;

    @Schema(description = "出版时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date publishTime;

    @Schema(description = "图书封面图片路径")
    private String cover;

    @Schema(description = "图书简介")
    @Column(columnDefinition = "TEXT") // 建议指定为 TEXT 类型以支持长文本
    private String introduction;

    /*
     * JSON 示例：
     * {
     * "isbn":"isbn",
     * "name":"name",
     * "author":"author",
     * "pages":300,
     * "translate":"无",
     * "publish":"出版社",
     * "price":35.5,
     * "size":10,
     * "type":"文学",
     * "publishTime":"2024-01-01T00:00:00",
     * "cover":"/images/upload/uuid.jpg",
     * "introduction":"这是一本好书..."
     * }
     */
}