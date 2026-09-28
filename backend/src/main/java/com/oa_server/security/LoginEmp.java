package com.oa_server.security;

import com.oa_server.module.emp.entity.Emp;
import lombok.Data;

/**
 * 登录员工信息封装（存于 SaSession）
 *
 * @author Alu
 * @date 2026-09-09
 */
@Data
public class LoginEmp {

    /**
     * 员工id
     */
    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色类型（0-普通员工，1-管理员）
     */
    private Integer roleType;

    /**
     * 账号状态（0-禁用，1-正常，2-待完善）
     */
    private Integer accountStatus;

    public LoginEmp() {
    }

    public LoginEmp(Emp emp) {
        this.id = emp.getId();
        this.email = emp.getEmail();
        this.roleType = emp.getRoleType();
        this.accountStatus = emp.getAccountStatus();
    }

}
