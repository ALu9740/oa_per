package com.oa_server.security;

import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 安全上下文工具类
 *
 * @author Alu
 * @date 2026-09-11
 */
public class SecurityUtils {
    private SecurityUtils(){}

    /**
     * 获取当前认证信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前登录员工
     *
     * @return LoginEmp
     * @throws BusinessException 未登录时抛出
     */
    public static LoginEmp getCurrentEmp() {
        Authentication authentication = getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ResultCode.LOGIN_EXPIRED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginEmp loginEmp) {
            return loginEmp;
        }
        throw new BusinessException(ResultCode.LOGIN_EXPIRED);
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentEmpId() {
        return getCurrentEmp().getId();
    }
}
