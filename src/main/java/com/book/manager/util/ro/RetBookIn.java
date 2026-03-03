package com.book.manager.util.ro;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "归还图书请求参数")
public class RetBookIn {

    @Schema(description = "用户ID", example = "1")
    private Integer userId;

    @Schema(description = "图书ID", example = "1001")
    private Integer bookId;
}

