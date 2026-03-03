package com.book.manager.util.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页返回对象")
public class PageOut {

    @Schema(description = "当前页", example = "1")
    private Integer currPage;

    @Schema(description = "每页条数", example = "10")
    private Integer pageSize;

    @Schema(description = "总记录数", example = "100")
    private Integer total;

    @Schema(description = "分页数据列表")
    private Object list;
}
