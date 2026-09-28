package com.oa_server.module.auth.service;

import cn.hutool.core.util.StrUtil;
import cloud.tianai.captcha.application.ImageCaptchaApplication;
import cloud.tianai.captcha.spring.plugins.secondary.SecondaryVerificationApplication;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 滑块验证码二次校验：业务接口调用前校验 token
 *
 * @author Alu
 * @date 2026-09-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CaptchaValidator {

    private final ImageCaptchaApplication imageCaptchaApplication;

    /**
     * 校验二次校验 token（一次性消费：校验成功即从 Redis 删除，防重放）
     */
    public void check(String captchaToken) {
        // secondary.enabled=true 时，Bean 实际类型是 SecondaryVerificationApplication
        if (!(imageCaptchaApplication instanceof SecondaryVerificationApplication app)) {
            log.error("[验证码] captcha.secondary.enabled 未开启，无法二次校验，请检查 application.yml");
            throw new BusinessException(ResultCode.SYSTEM_ERROR);
        }
        if (StrUtil.isBlank(captchaToken) || !app.secondaryVerification(captchaToken)) {
            throw new BusinessException(ResultCode.CAPTCHA_INVALID);
        }
    }
}