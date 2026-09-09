package com.oa_server.security;

import com.oa_server.module.emp.entity.Emp;
import com.oa_server.module.emp.mapper.EmpMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security UserDetailsService
 *
 * @author Alu
 * @date 2026-09-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final EmpMapper empMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("[加载用户] email={}", email);
        Emp emp = empMapper.findByEmail(email);
        if (emp == null) {
            throw new UsernameNotFoundException("用户不存在: " + email);
        }
        return new LoginEmp(emp);
    }
}
