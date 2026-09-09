package com.oa_server.module.emp.entity;


import com.baomidou.mybatisplus.annotation.TableName;
import com.oa_server.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 员工实体
 *
 * @author Alu
 * @date 2026-09-09
 */
@Data
@EqualsAndHashCode(callSuper = true) // 包含父类字段
@TableName("emp")
public class Emp extends BaseEntity {
    /**
     * 员工编号(唯一)
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
     * 手机号
     */
     private String phone;

    /**
     * 邮箱
     */
     private String email;

    /**
     * 头像URL
     */
     private String avatar;

    /**
     * 密码(加密存储)
     */
     private String password;

    /**
     * 部门ID
     */
     private Long deptId;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 入职时间
     */
     private LocalDate hireDate;

    /**
     * 角色类型：0-普通员工 1-管理员
     */
     private Integer roleType;

    /**
     * 账号状态 ：0-禁用 1-正常
     */
     private Integer accountStatus;


}
