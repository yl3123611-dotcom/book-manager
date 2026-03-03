package com.book.manager.util.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * 图书出参对象
 */
@Data
@Schema(description = "图书返回对象")
public class BookOut {

    @Schema(description = "书籍ID")
    private Integer id;

    @Schema(description = "书籍ISBN编码")
    private String isbn;

    @Schema(description = "书名")
    private String name;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "页数")
    private Integer pages;

    @Schema(description = "翻译")
    private String translate;

    @Schema(description = "出版社")
    private String publish;

    @Schema(description = "定价")
    private Double price;

    @Schema(description = "库存")
    private Integer size;

    @Schema(description = "分类")
    private String type;

    @Schema(description = "出版时间")
    private String publishTime;

    // ✅ 关键：封面路径（否则列表页拿不到 value.cover）
    @Schema(description = "图书封面图片路径")
    private String cover;

    // （可选）如果你详情页也要显示简介，建议也带上
    @Schema(description = "图书简介")
    private String introduction;

    @Schema(description = "馆藏位置信息（floorName/shelfCode/shelfName/layerNo/cellNo/callNo 等）")
    private Map<String, Object> location;
}
