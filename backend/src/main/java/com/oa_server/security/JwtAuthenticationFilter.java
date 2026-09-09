package com.oa_server.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import com.oa_server.common.result.Result;
import com.oa_server.common.result.ResultCode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * JWT 认证过滤器
 *
 * @author Alu
 * @date 2026-09-09
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = resolveToken(request);
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                String email = claims.getSubject();
                String tokenType = (String) claims.get("type");
                // 仅处理 ACCESS 类型 Token
                if ("ACCESS".equals(tokenType) && StringUtils.hasText(email)) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                    // 校验登录版本号：不匹配说明账号已在其他设备登录，强制下线
                    if (userDetails instanceof LoginEmp loginEmp) {
                        // 封禁检查：用户被封禁后立即踢下线
                        if (loginEmp.getAccountStatus() != null && loginEmp.getAccountStatus() == 0) {
                            log.info("[JWT] 账号已被封禁，强制下线: userId={}", loginEmp.getId());
                            writeBannedResponse(response);
                            return;
                        }

                    }

                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.warn("[JWT] 认证过滤器异常: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 写入"账号已被封禁"的 401 响应
     */
    private void writeBannedResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        Result<Void> result = Result.error(ResultCode.ACCOUNT_DISABLED, "账号已被封禁");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }

    /**
     * 从请求头中解析 Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
