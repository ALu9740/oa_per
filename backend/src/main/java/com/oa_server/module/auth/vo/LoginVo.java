package com.oa_server.module.auth.vo;

import com.oa_server.module.emp.vo.EmpVO;
import lombok.Data;

/**
 * 登录 VO
 *
 * @author Alu
 * @date 2026-09-09
 */
@Data
public class LoginVo {
    /**
     * 访问 Token
     */
    private String accessToken;

    /**
     * 刷新 Token
     */
    private String refreshToken;

    /**
     * Token 类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间(毫秒)
     */
    private Long expiresIn;

    /**
     * 员工信息
     */
    private EmpVO empVO;
}
