package com.oa_server.module.emp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工头像信息状态枚举
 *
 * @author Alu
 * @date 2026-09-11
 */
@Getter
@AllArgsConstructor
public enum EmpAvatarEnum {

    AVATAR_DATA_EMPTY(10001, "头像数据不能为空"),
    AVATAR_BASE64_DECODE_FAILED(10002, "头像 Base64 解码失败"),
    AVATAR_SIZE_EXCEEDED(10003, "头像大小不能超过 2MB"),
    AVATAR_UPLOAD_FAILED(10004, "头像上传失败，请稍后重试");

    private final Integer code;
    private final String message;
}
