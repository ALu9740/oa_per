package com.oa_server.module.admin.jobs.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位管理 VO
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
public class AdminJobVO {
    /**
     * 职位ID
     */
    private Long id;

    /**
     * 职位名称
     */
    private String jobName;

    /**
     * 排序（越小越靠前）
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
