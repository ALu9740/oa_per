package com.oa_server.module.emp.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 角色类型枚举
 *
 * @author Alu
 * @date 2026-09-10
 */
@Getter
@AllArgsConstructor
public enum EmpRoleTypeEnum {
    /**
     * 角色类型：0-普通员工 1-管理员
     */
    NORMAL(0, "普通员工"),
    ADMIN(1, "管理员");
    private final Integer code;
    private final String message;
}
