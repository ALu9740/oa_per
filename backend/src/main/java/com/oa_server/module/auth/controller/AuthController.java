package com.oa_server.module.auth.controller;

import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.Result;
import com.oa_server.common.result.ResultCode;
import com.oa_server.module.auth.dto.CompleteProfileDTO;
import com.oa_server.module.auth.dto.RegisterDTO;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.service.AuthService;
import com.oa_server.module.auth.vo.LoginVo;
import com.oa_server.module.emp.service.EmpService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

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

    private static final String REGISTER_LIMIT_PREFIX = "auth:register:limit:";
    private static final Duration REGISTER_LIMIT_TTL = Duration.ofHours(1);
    private static final int REGISTER_MAX_COUNT = 5;

    private final AuthService authService;
    private final StringRedisTemplate stringRedisTemplate;
    private final EmpService empService;

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

    /**
     * 员工注册
     */
    @PostMapping("/register")
    public Result<LoginVo> register(@Valid @RequestBody RegisterDTO registerDTO, HttpServletRequest request) {
        // IP 限流
        String ip = getClientIp(request);
        String limitKey = REGISTER_LIMIT_PREFIX + ip;
        Long count = stringRedisTemplate.opsForValue().increment(limitKey);
        if (count != null && count == 1) {
            stringRedisTemplate.expire(limitKey, REGISTER_LIMIT_TTL);
        }
        if (count != null && count > REGISTER_MAX_COUNT) {
            throw new BusinessException(ResultCode.TOO_MANY_REQUESTS);
        }

        return Result.success(authService.register(registerDTO));
    }

    /**
     * 完善资料
     */
    @PutMapping("/complete-profile")
    public Result<Void> completeProfile(@Valid @RequestBody CompleteProfileDTO completeProfileDTO) {
        empService.completeProfile(completeProfileDTO);
        return Result.success();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多层代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
