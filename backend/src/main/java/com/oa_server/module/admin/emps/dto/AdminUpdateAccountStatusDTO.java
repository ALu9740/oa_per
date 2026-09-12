package com.oa_server.module.admin.emps.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新账号状态 DTO
 *
 * @author Alu
 * @date 2026-09-12
 */
@Data
public class AdminUpdateAccountStatusDTO {

    @NotNull(message = "员工ID不能为空")
    private Long id;

    /**
     * 账号状态 0-禁用 1-正常 2-待完善
     */
    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "账号状态值非法")
    @Max(value = 2, message = "账号状态值非法")
    private Integer accountStatus;
}
