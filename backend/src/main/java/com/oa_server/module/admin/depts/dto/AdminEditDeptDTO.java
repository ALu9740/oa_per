package com.oa_server.module.admin.depts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑部门信息 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminEditDeptDTO {

    @NotNull(message = "部门ID不能为空")
    private Long id;

    @NotBlank(message = "部门名称不能为空")
    private String name;

    private String description;
}
