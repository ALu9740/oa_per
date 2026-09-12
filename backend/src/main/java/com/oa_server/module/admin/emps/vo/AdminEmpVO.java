package com.oa_server.module.admin.emps.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工管理 VO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminEmpVO {
    /**
     * 员工编号
     */
    private String empNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 （0-女 1-男）
     */
    private Integer gender;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 入职时间
     */
    private LocalDate hireDate;

    /**
     * 账号状态 ：0-禁用 1-正常 2-待完善
     */
    private Integer accountStatus;
}
