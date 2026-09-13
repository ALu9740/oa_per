package com.oa_server.module.admin.jobs.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除职位 DTO
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
public class AdminBatchDeleteJobDTO {
    /**
     * 职位ID列表
     */
    @NotEmpty(message = "职位ID列表不能为空")
    private List<Long> jobIds;
}
