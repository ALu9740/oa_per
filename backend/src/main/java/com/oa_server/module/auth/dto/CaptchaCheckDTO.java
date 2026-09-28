package com.oa_server.module.auth.dto;

import cloud.tianai.captcha.validator.common.model.dto.ImageCaptchaTrack;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 滑块验证码校验 DTO：id + 前端回传的滑动轨迹
 *
 * @author Alu
 * @date 2026-09-24
 */
@Data
public class CaptchaCheckDTO {

    @NotBlank(message = "验证码ID不能为空")
    private String id;

    @NotNull(message = "滑动轨迹不能为空")
    private ImageCaptchaTrack data;
}