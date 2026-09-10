package com.oa_server.module.auth.service;

import com.oa_server.module.auth.dto.RegisterDTO;
import com.oa_server.module.auth.dto.SendCodeDTO;
import com.oa_server.module.auth.vo.LoginVo;

/**
 * 认证服务接口
 *
 * @author Alu
 * @date 2026-09-09
 */
public interface AuthService {

    /**
     * 发送验证码
     *
     * @param sendCodeDTO 发送验证信息
     */
    void sendCode(SendCodeDTO sendCodeDTO);

    /**
     * 校验验证码
     *
     * @param email 邮箱
     * @param code 验证码
     * @return 是否校验通过
     */
    Boolean verifyCode(String email, String code);

    /**
     * 注册
     *
     * @param registerDTO 注册信息
     * @return 登录信息
     */
    LoginVo register(RegisterDTO registerDTO);
}
