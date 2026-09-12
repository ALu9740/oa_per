package com.oa_server.module.admin.depts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增部门 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminAddDeptDTO {

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 32, message = "部门名称长度不能超过32个字符")
    private String deptName;

    /**
     * 部门描述
     */
    private String deptDesc;
}
