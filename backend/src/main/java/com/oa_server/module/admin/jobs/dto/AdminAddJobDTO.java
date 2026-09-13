package com.oa_server.module.admin.jobs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 新增职位 DTO
 *
 * @author Alu
 * @date 2026-09-13
 */
@Data
public class AdminAddJobDTO {

    /**
     * 职位名称
     */
    @NotBlank(message = "职位名称不能为空")
    @Size(max = 32, message = "职位名称长度不能超过32个字符")
    private String jobName;

    /**
     * 排序
     */
    private Integer sort;

}
