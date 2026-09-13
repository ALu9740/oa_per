package com.oa_server.module.admin.jobs.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位列表查询 DTO
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
public class AdminJobQueryDTO {
    /**
     * 职位名称
     */
    private String jobName;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

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
