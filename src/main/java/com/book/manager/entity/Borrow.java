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
 * @Description 借阅表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Entity
@Table(name = "borrow")
@Schema(description = "借阅记录实体")
public class Borrow {

    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "图书ID")
    private Integer bookId;

    @Schema(description = "借阅时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createTime;

    @Schema(description = "应归还时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Schema(description = "实际归还时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updateTime;

    @Schema(description = "是否归还：0 已归还 / 1 未归还")
    private Integer ret;

    @Schema(description = "上次提醒时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastNotify;

    /*
     * JSON 示例：
     * {
     *   "userId": 1,
     *   "bookId": 10,
     *   "createTime": "2024-01-01T10:00:00",
     *   "endTime": "2024-01-15T10:00:00",
     *   "updateTime": "2024-01-10T09:30:00",
     *   "ret": 1
     * }
     */
}
