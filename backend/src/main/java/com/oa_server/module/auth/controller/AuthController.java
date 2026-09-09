package com.oa_server.module.auth.controller;

import com.oa_server.common.result.Result;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/send-code")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeDTO sendCodeDTO) {
        authService.sendCode(sendCodeDTO);
        return Result.success("验证码已发送", null);
    }

    /**
     * 校验验证码
     */
    @GetMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestParam String email, @RequestParam String code) {
        return Result.success(authService.verifyCode(email, code));
    }


}
