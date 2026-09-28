package com.oa_server.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 鉴权配置
 *
 * @author Alu
 * @date 2026-09-28
 */
@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            // 登录校验：拦截 /api/**，排除白名单
            SaRouter.match("/api/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/send-code",
                            "/api/auth/verify-code",
                            "/api/auth/reset-password",
                            "/api/auth/complete-profile",
                            "/api/auth/refresh",
                            "/api/auth/logout",
                            "/api/captcha/generation",
                            "/api/captcha/check"
                    )
                    .check(r -> StpUtil.checkLogin());

            // 管理员专属：/api/admin/**
            SaRouter.match("/api/admin/**")
                    .check(r -> StpUtil.checkRole("admin"));

            // 管理员智能体、知识库管理
            SaRouter.match("/api/ai/agent/**", "/api/ai/kb/**")
                    .check(r -> StpUtil.checkRole("admin"));
        })).addPathPatterns("/**");
    }
}