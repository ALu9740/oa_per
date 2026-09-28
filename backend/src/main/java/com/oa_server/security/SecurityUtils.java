package com.oa_server.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.oa_server.common.exception.BusinessException;
import com.oa_server.common.result.ResultCode;

/**
 * 安全上下文工具类（基于 Sa-Token）
 *
 * @author Alu
 * @date 2026-09-11
 */
public class SecurityUtils {
    private SecurityUtils() {}

    /**
     * 获取当前登录员工（登录时存入 SaSession）
     */
    public static LoginEmp getCurrentEmp() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ResultCode.LOGIN_EXPIRED);
        }
        SaSession session = StpUtil.getSession();
        LoginEmp loginEmp = (LoginEmp) session.get("loginEmp");
        if (loginEmp == null) {
            throw new BusinessException(ResultCode.LOGIN_EXPIRED);
        }
        return loginEmp;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentEmpId() {
        return getCurrentEmp().getId();
    }
}