package com.book.manager.util.http;

/**
 * @Description 响应状态码枚举类
 */
public enum CodeEnum {

    /** 请求成功 */
    SUCCESS(200, "成功!"),

    /** 您已借阅过该图书, 且未归还 */
    BOOK_BORROWED(300, "您已借阅过该图书, 且未归还!"),

    /** 图书库存不够,无法借阅 */
    BOOK_NOT_ENOUGH(301, "图书库存不够,无法借阅!"),

    /** 用户可借数量不够 */
    USER_NOT_ENOUGH(302, "用户可借数量不够,无法借阅!"),

    /** 找不到资源 */
    NOT_FOUND(404, "找不到资源!"),

    /** 请求参数错误 */
    PARAM_ERROR(444, "请求参数错误!"),

    /** 用户名或密码错误 */
    NAME_OR_PASS_ERROR(445, "用户名或密码错误!"),

    /** 找不到用户 */
    USER_NOT_FOUND(446, "找不到用户!"),

    /** 服务器发生异常 */
    FAIL(500, "服务器发生异常!"),

    /** 系统异常（AI / 未知异常） */
    SYSTEM_ERROR(501, "系统异常，请稍后再试!"),

    /** 图片上传失败 */
    UPLOAD_ERROR(502, "图片上传失败!");

    private final int code;
    private final String msg;

    CodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
