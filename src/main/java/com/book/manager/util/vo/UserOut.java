package com.book.manager.util.vo;

import com.book.manager.entity.Users;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @Description 用户返回对象
 */
@Data
@Schema(description = "用户返回对象")
public class UserOut extends Users {

    @Schema(description = "身份（中文：学生 / 教师 / 社会人士 / 管理员）")
    private String ident;

    @Schema(description = "生日（yyyy-MM-dd 格式）")
    private String birth;
}

