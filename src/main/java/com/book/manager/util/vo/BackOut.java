package com.book.manager.util.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description 归还 VO 对象
 */
@Data
@Schema(description = "归还图书返回对象")
public class BackOut extends BookOut {

    @Schema(description = "借阅时间（yyyy-MM-dd）")
    private String borrowTime;

    @Schema(description = "应还时间（yyyy-MM-dd）")
    private String endTime;

    @Schema(description = "是否逾期（是 / 否）")
    private String late;

    private Integer borrowId; // 借阅记录ID
    private Integer lateDays; // 逾期天数
}
