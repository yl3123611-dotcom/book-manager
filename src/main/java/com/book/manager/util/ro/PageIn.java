package com.book.manager.util.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页查询入参")
public class PageIn {

    /** 搜索关键字 */
    @Schema(description = "搜索关键字")
    private String keyword;

    /** 当前页 */
    @Schema(description = "当前页", example = "1")
    private Integer currPage;

    /** 当前页条数 */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;
}
