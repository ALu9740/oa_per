package com.oa_server.module.admin.jobs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 编辑职位信息 DTO
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
public class AdminEditJobDTO {

    @NotNull(message = "职位ID不能为空")
    private Long id;

    @NotBlank(message = "职位名称不能为空")
    private String name;

    private Integer sort;
}
