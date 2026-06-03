package com.smarttask.common.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token 工具类
 *
 * JWT（JSON Web Token）：一种跨域认证方案，由三部分组成：
 *   Header（头部）+ Payload（负载）+ Signature（签名）
 *   服务端无需存储 Token，通过签名验证即可确认用户身份（无状态认证）
 *
 * 属性:
 *   secret    - 签名密钥，用于生成和验证 JWT
 *   expiration - Token 过期时间（毫秒）
 *
 * 方法:
 *   generateToken    - 生成 JWT Token，返回 String
 *   getUserIdFromToken - 从 Token 中解析出用户ID，返回 Long
 *   validateToken    - 验证 Token 是否有效，返回 boolean
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * 生成 JWT Token
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return 签名的JWT字符串
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userId.toString())   // 设置主题（用户ID）
                .claim("username", username)  // 自定义声明：用户名
                .claim("role", role)          // 自定义声明：用户角色
                .issuedAt(now)                // 签发时间
                .expiration(expiryDate)       // 过期时间
                .signWith(getSigningKey())    // 使用 HMAC-SHA256 算法签名
                .compact();
    }

    /**
     * 从 Token 中提取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从 Token 中提取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 从 Token 中提取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 验证 Token 是否有效
     * @param token JWT字符串
     * @return true=有效, false=无效或已过期
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 解析 JWT Token
     * @Claims: JWT 的载荷部分，包含用户数据和声明信息
     */
    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从 Base64 编码的密钥字符串中生成 HMAC-SHA256 签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(
                java.util.Base64.getEncoder().encodeToString(secret.getBytes())
        );
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
