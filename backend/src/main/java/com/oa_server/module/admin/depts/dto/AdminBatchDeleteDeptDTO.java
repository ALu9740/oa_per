package com.oa_server.module.admin.depts.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量删除部门 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminBatchDeleteDeptDTO {
    /**
     * 部门ID列表
     */
    @NotEmpty(message = "部门ID列表不能为空")
    private List<Long> deptIds;
}
