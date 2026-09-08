package com.oa_server.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 系统响应状态码枚举
 *
 * @author Alu
 * @date 2026-09-09
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    /* 成功 */
    SUCCESS(200, "操作成功"),

    /* 通用错误 1xxx */
    FAIL(1000, "操作失败"),
    PARAM_INVALID(1001, "参数校验失败"),
    LOGIN_EXPIRED(1002, "登录失效，请重新登录"),
    FORBIDDEN(1003, "无权限访问"),
    NOT_FOUND(1004, "资源不存在"),
    METHOD_NOT_ALLOWED(1005, "请求方法不支持"),
    SYSTEM_ERROR(1006, "系统繁忙，请稍后再试"),
    TOO_MANY_REQUESTS(1007, "请求过于频繁，请稍后再试");

    private final Integer code;
    private final String message;
}
