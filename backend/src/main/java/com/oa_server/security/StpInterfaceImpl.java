package com.oa_server.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 角色数据源
 *
 * @author Alu
 * @date 2026-09-28
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取登录员工的角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        LoginEmp loginEmp = (LoginEmp) session.get("loginEmp");
        if (loginEmp == null) {
            return Collections.emptyList();
        }
        boolean isAdmin = loginEmp.getRoleType() != null && loginEmp.getRoleType() == 1;
        return isAdmin ? List.of("admin") : List.of("emp");
    }

    /**
     * 获取登录员工的权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 本系统只区分角色，不做细粒度权限，返回空即可
        return Collections.emptyList();
    }
}