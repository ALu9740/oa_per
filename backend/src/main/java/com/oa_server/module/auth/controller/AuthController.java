package com.oa_server.module.auth.controller;

import com.oa_server.common.result.Result;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口
 *
 * @author Alu
 * @date 2026-09-09
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 发送验证码
     */
    @PostMapping("/sendCode")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        authService.sendCode(sendCodeDTO);
        return Result.success("验证码已发送", null);
    }
}
