package com.oa_server.module.emp.vo;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工 VO
 *
 * @author Alu
 * @date 2026-09-09
 */
@Data
public class EmpVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 员工编号
     */
    private String empNo;

    /**
     * 姓名
     */
    private String name;

    /**
     * 性别 (0-女 1-男)
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
     * 头像URL
     */
    private String avatar;

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
     * 角色类型：0-普通员工 1-管理员
     */
    private Integer roleType;


    /**
     * 账号状态 (0-禁用 1-启用 2-未完善)
     */
    private Integer accountStatus;
}
