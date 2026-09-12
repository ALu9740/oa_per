package com.oa_server.module.admin.emps.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除员工 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminBatchDeleteEmpDTO {

    /**
     * 员工ID列表
     */
    @NotEmpty(message = "员工ID列表不能为空")
    private List<Long> empIds;
}
