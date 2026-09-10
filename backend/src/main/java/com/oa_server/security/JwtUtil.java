package com.oa_server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 *
 * @author Alu
 * @date 2026-09-09
 */
@Slf4j
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:7200000}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        log.info("[JWT] 密钥初始化完成, access={}ms, refresh={}ms", accessTokenExpiration, refreshTokenExpiration);
    }

    /**
     * 生成访问 Token
     *
     * @param empID       员工ID
     * @param email        邮箱(作为 subject)
     * @return accessToken
     */
    public String generateAccessToken(Long empID, String email) {
        return buildToken(empID, email, accessTokenExpiration, "ACCESS");
    }

    /**
     * 生成刷新 Token
     *
     * @param empID       员工ID
     * @param email        邮箱
     * @return refreshToken
     */
    public String generateRefreshToken(Long empID, String email) {
        return buildToken(empID, email, refreshTokenExpiration, "REFRESH");
    }



    /**
     * 构建 Token
     */
    private String buildToken(Long empID, String email, long expiration, String type) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("eid", empID);
        claims.put("type", type);
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .claims(claims)
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    /**
     * 解析 Token
     *
     * @param token JWT 字符串
     * @return Claims
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Token 中获取员工ID
     */
    public Long getEmpIdFromToken(String token) {
        Claims claims = parseToken(token);
        Object eid = claims.get("eid");
        if (eid instanceof Number) {
            return ((Number) eid).longValue();
        }
        return null;
    }

    /**
     * 从 Token 中获取邮箱(即 subject)
     */
    public String getEmailFromToken(String token) {
        return parseToken(token).getSubject();
    }


    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            log.debug("[JWT] Token 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            return parseToken(token).getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 从已过期的 Token 中提取员工ID（用于 Token 过期时清理员工在线状态）。
     * ExpiredJwtException 会携带过期前的 Claims 信息。
     *
     * @param token JWT 字符串
     * @return 员工ID（仅当 Token 是因过期而失效时返回，其他异常返回 null）
     */
    public Long getEmpIdFromExpiredToken(String token) {
        try {
            parseToken(token);
            return null; // Token 未过期
        } catch (ExpiredJwtException e) {
            Claims claims = e.getClaims();
            Object eid = claims.get("eid");
            if (eid instanceof Number) {
                return ((Number) eid).longValue();
            }
            return null;
        } catch (Exception e) {
            return null; // 其他异常（签名错误、格式错误等）
        }
    }

    /**
     * 获取访问 Token 过期时间(毫秒)
     */
    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    /** 获取 Token 剩余有效时间(毫秒) */
    public long getRemainingTime(String token) {
        Claims claims = parseToken(token);
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 判断是否为 REFRESH 类型 Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "REFRESH".equals(claims.get("type"));
        } catch (Exception e) {
            log.debug("[JWT] Token 类型判断失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断是否为 ACCESS 类型 Token
     */
    public boolean isAccessToken(String token) {
        try {
            Claims claims = parseToken(token);
            return "ACCESS".equals(claims.get("type"));
        } catch (Exception e) {
            log.debug("[JWT] Token 类型判断失败: {}", e.getMessage());
            return false;
        }
    }
}
