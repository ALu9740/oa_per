package com.oa_server.security;

import com.oa_server.module.emp.entity.Emp;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 登录员工信息封装
 *
 * @author Alu
 * @date 2026-09-09
 */
@Data
public class LoginEmp implements UserDetails {

    /**
     * 员工id
     */
    private Long id;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 角色类型（0-普通员工，1-管理员）
     */
    private Integer roleType;

    /**
     * 账号状态（0-禁用，1-正常，2-待完善）
     */
    private Integer accountStatus;

    public LoginEmp(Emp emp) {
        this.id = emp.getId();
        this.email = emp.getEmail();
        this.password = emp.getPassword();
        this.avatar = emp.getAvatar();
        this.roleType = emp.getRoleType();
        this.accountStatus = emp.getAccountStatus();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_TYPE_" + (roleType == 0 ? "Emp" : "Admin");
        return Collections.singletonList(new SimpleGrantedAuthority(authority));
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return accountStatus == 1;
    }
}
