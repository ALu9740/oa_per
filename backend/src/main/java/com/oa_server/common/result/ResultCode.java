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
    TOO_MANY_REQUESTS(1007, "请求过于频繁，请稍后再试"),

    /* 认证模块 2xxx */
    CODE_SEND_TOO_FREQUENT(2001, "验证码发送过于频繁，请60秒后再试"),
    ACCOUNT_DISABLED(2002, "账号已被禁用"),
    ACCOUNT_LOGIN_ELSEWHERE(2003, "当前账号已在其他设备登录，您已被强制下线"),
    EMAIL_SEND_FAILED(2004, "邮件发送失败，请检查邮箱地址或联系管理员"),
    CODE_INVALID(2005, "验证码无效或已过期"),
    CODE_NOT_MATCH(2006, "验证码不正确"),
    EMAIL_EXISTS(2007, "邮箱已被注册"),
    ACCOUNT_LOCKED(2008, "账号已被锁定，请15分钟后再试"),
    EMAIL_OR_PASSWORD_ERROR(2009, "邮箱或密码错误"),
    EMAIL_NOT_FOUND(2010, "邮箱不存在"),
    TOKEN_INVALID(2011, "Token 无效"),
    PHONE_EXISTS(2012, "手机号已被注册"),

    /* 员工模块 3xxx */
    EMP_NOT_FOUND(3001, "员工不存在"),
    OLD_PASSWORD_ERROR(3002, "旧密码不正确"),
    ;

    private final Integer code;
    private final String message;
}
