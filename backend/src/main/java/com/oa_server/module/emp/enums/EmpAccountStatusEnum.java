package com.oa_server.module.emp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 员工账号状态枚举
 *
 * @author Alu
 * @date 2026-09-10
 */
@Getter
@AllArgsConstructor
public enum EmpAccountStatusEnum {
    /**
     * 账号状态：0-禁用 1-正常 2-待完善
     */
    DISABLED(0, "禁用"),
    NORMAL(1, "正常"),
    PENDING(2, "待完善");
    private final Integer code;
    private final String message;
}
