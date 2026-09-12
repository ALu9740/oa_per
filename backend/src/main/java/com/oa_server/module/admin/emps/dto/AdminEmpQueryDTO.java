package com.oa_server.module.admin.emps.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 员工列表查询条件 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminEmpQueryDTO {

    /**
     * 姓名
     */
    private String name;

    /**
     * 部门ID
     */
    private Long deptId;

    /**
     * 职位ID
     */
    private Long jobId;

    /**
     * 入职时间 - 起始
     */
    private LocalDate hireDateStart;

    /**
     * 入职时间 - 结束
     */
    private LocalDate hireDateEnd;

    /**
     * 账号状态（0-禁用 1-正常 2-待完善）
     */
    private Integer accountStatus;

    /**
     * 页码（默认 1）
     */
    private Long page = 1L;

    /**
     * 每页大小（默认 10）
     */
    private Long size = 10L;
}