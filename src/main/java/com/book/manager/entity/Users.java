package com.book.manager.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DynamicUpdate
@DynamicInsert
@Entity
@Table(name = "users")
public class Users {

    @Schema(description = "主键ID")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "用户名")
    @Column(unique = true, nullable = false)
    private String username;

    @Schema(description = "密码")
    @Column(nullable = false)
    private String password;

    @Schema(description = "生日")
    @Temporal(TemporalType.TIMESTAMP)
    private Date birthday;

    @Schema(description = "是否为管理员：0 管理员 / 1 普通用户（按你原项目逻辑）")
    private Integer isAdmin = 0;

    @Schema(description = "电话")
    private String tel;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "可借数量")
    private Integer size = 5;

    @Schema(description = "身份：0 学生,1 教师,2 校外人士,3 管理员")
    private Integer identity = 0;

    @Schema(description = "座位违约次数")
    private Integer seatViolationCount = 0;

    @Schema(description = "座位预约封禁截止时间")
    @Temporal(TemporalType.TIMESTAMP)
    private Date seatBanUntil;
}
