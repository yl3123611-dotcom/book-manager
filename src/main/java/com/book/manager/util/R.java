package com.book.manager.util;

import com.book.manager.util.http.CodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * @Description 返回对象
 */
@Schema(description = "统一接口返回对象")
public class R implements Serializable {

    @Schema(description = "响应码")
    private Integer code;

    @Schema(description = "响应信息")
    private String msg;

    @Schema(description = "响应数据")
    private Object data;

    public R() {
    }

    public R(String msg, String data) {
        this.msg = msg;
        this.data = data;
    }

    public R(Integer code, String msg, String data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public R(CodeEnum codeEnum, Object data) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
        this.data = data;
    }

    public R(CodeEnum codeEnum) {
        this.code = codeEnum.getCode();
        this.msg = codeEnum.getMsg();
    }

    public static R success(CodeEnum codeEnum, Object data) {
        return new R(codeEnum, data);
    }

    public static R success(CodeEnum codeEnum) {
        return new R(codeEnum);
    }

    public static R fail(CodeEnum codeEnum) {
        return new R(codeEnum);
    }

    /**
     * 成功（自定义提示信息）
     */
    public static R successMsg(String msg) {
        R r = new R();
        r.code = CodeEnum.SUCCESS.getCode();
        r.msg = msg;
        return r;
    }

    /**
     * 成功（自定义提示信息 + 数据）
     */
    public static R successMsg(String msg, Object data) {
        R r = new R();
        r.code = CodeEnum.SUCCESS.getCode();
        r.msg = msg;
        r.data = data;
        return r;
    }

    /**
     * 失败（自定义提示信息）
     */
    public static R failMsg(String msg) {
        R r = new R();
        r.code = CodeEnum.FAIL.getCode();
        r.msg = msg;
        return r;
    }

    /**
     * 参数错误（自定义提示信息）
     */
    public static R paramError(String msg) {
        R r = new R();
        r.code = CodeEnum.PARAM_ERROR.getCode();
        r.msg = msg;
        return r;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
