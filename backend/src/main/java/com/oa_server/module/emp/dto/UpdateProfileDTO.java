package com.oa_server.module.emp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新员工资料 DTO
 *
 * @author Alu
 * @date 2026-09-11
 */
@Data
public class UpdateProfileDTO {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 32, message = "姓名长度不能超过32")
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
