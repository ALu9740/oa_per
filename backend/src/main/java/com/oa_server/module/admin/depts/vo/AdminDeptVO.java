package com.oa_server.module.admin.depts.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门管理 VO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminDeptVO {
    /**
     * 部门ID
     */
    private Long id;

    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
