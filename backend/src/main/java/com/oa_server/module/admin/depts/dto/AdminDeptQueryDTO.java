package com.oa_server.module.admin.depts.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 部门列表查询 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminDeptQueryDTO {
    /**
     * 部门名称 (模糊查询)
     */
    private String name;

    /**
     * 创建时间 - 起始
     */
    private LocalDateTime createdAtStart;

    /**
     * 创建时间 - 结束
     */
    private LocalDateTime createdAtEnd;

    /**
     * 更新时间 - 起始
     */
    private LocalDateTime updatedAtStart;

    /**
     * 更新时间 - 结束
     */
    private LocalDateTime updatedAtEnd;

    /**
     * 页码（默认 1）
     */
    private Long page = 1L;

    /**
     * 每页大小（默认 10）
     */
    private Long size = 10L;
}
