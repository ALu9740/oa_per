package com.oa_server.module.auth.service;

import com.oa_server.module.auth.dto.SendCodeDTO;

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
}
