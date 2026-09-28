package com.oa_server.module.auth.controller;

import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.application.vo.ImageCaptchaVO;
import cloud.tianai.captcha.common.constant.CaptchaTypeConstant;
import cloud.tianai.captcha.common.response.ApiResponse;
import com.oa_server.module.auth.dto.CaptchaCheckDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * 滑块验证码接口（供前端 tac SDK 调用）
 *
 * @author Alu
 * @date 2026-09-24
 */
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final ImageCaptchaApplication imageCaptchaApplication;

    /**
     * 生成滑块验证码
     */
    @PostMapping("/generation")
    public ApiResponse<ImageCaptchaVO> generation() {
        return imageCaptchaApplication.generateCaptcha(CaptchaTypeConstant.SLIDER);
    }

    /**
     * 一次校验：校验滑动轨迹
     */
    @PostMapping("/check")
    public ApiResponse<?> check(@Valid @RequestBody CaptchaCheckDTO dto) {
        ApiResponse<?> response = imageCaptchaApplication.matching(dto.getId(), dto.getData());
        if (response.isSuccess()) {
            // secondary.enabled=true 时，上面的 matching 成功已自动在 Redis 写入二次校验凭证
            // 将 captchaId 作为 token 发给前端，前端提交业务接口时带上
            return ApiResponse.ofSuccess(Collections.singletonMap("token", dto.getId()));
        }
        return response;
    }
}