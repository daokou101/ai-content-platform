package com.pzhu.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具类
 * <p>
 * 【什么是 JWT？】
 * JWT（Json Web Token）是一种"令牌"，用户登录成功后，后端会生成一个 JWT 返回给前端。
 * 前端之后每次请求都带上这个令牌，后端通过拦截器校验令牌是否有效，从而知道"你是谁"。
 * <p>
 * 【JWT 的结构】
 * 一个 JWT 由三部分组成：Header（头部） + Payload（载荷） + Signature（签名）
 * 比如：xxxxx.yyyyy.zzzzz
 * <p>
 * 【为什么用 JWT？】
 * 传统的 Session 登录方式需要把用户信息存在服务器内存中，如果部署多台服务器就不好共享。
 * JWT 是无状态的，服务器只需要验证签名，不需要存储登录信息，适合前后端分离和分布式系统。
 * <p>
 * 【这个类的作用】
 * 封装了 JWT 的"生成"和"解析"两个核心操作。
 * - generateToken()：用户登录成功后调用，生成令牌
 * - parseToken()：拦截器中调用，解析令牌获取用户信息
 */
public class JwtUtils {

    /**
     * 签名密钥（加密的秘钥字符串）
     * 注意：实际项目中这个密钥应该放在配置文件中，不要硬编码在代码里
     * 这里的密钥至少需要 256 位（32 个字符），因为 JWT 使用 HMAC-SHA256 算法
     */
    private static final String SECRET_STRING = "TliasSecretKeyForJwtTokenGeneration2024VeryLong";

    /**
     * 令牌过期时间：12 小时（单位：毫秒）
     * 12 * 60 * 60 * 1000 = 43200000 毫秒
     * 过期后用户需要重新登录
     */
    private static final long EXPIRATION = 12 * 60 * 60 * 1000L;

    // 将字符串密钥转换成 JWT 需要的 SecretKey 对象
    // Keys.hmacShaKeyFor() 会根据字符串的字节数组生成一个符合 HMAC-SHA 算法的密钥
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_STRING.getBytes());

    /**
     * 生成 JWT 令牌
     *
     * @param claims 要存放到令牌中的"声明"信息（比如用户 id、用户名等）
     * @return 生成的 JWT 字符串
     *
     * 【用法示例】
     * Map<String, Object> claims = new HashMap<>();
     * claims.put("id", 1);
     * claims.put("username", "admin");
     * String token = JwtUtils.generateToken(claims);
     */
    public static String generateToken(Map<String, Object> claims) {
        return Jwts.builder()
                // 设置自定义的声明（payload 部分），存放用户信息
                .claims(claims)
                // 设置签发时间（当前时间）
                .issuedAt(new Date())
                // 设置过期时间（当前时间 + 12小时）
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                // 指定签名算法和密钥，用于防止令牌被篡改
                .signWith(SECRET_KEY)
                // 生成最终的 JWT 字符串
                .compact();
    }

    /**
     * 解析 JWT 令牌，获取其中存放的数据
     *
     * @param token JWT 字符串
     * @return Claims 对象（里面包含了生成令牌时存入的用户信息）
     *
     * 【说明】
     * 如果令牌被篡改或已过期，解析时会抛出异常，拦截器会捕获该异常并返回未授权错误
     */
    public static Claims parseToken(String token) {
        return Jwts.parser()
                // 指定验证签名用的密钥
                .verifyWith(SECRET_KEY)
                .build()
                // 解析并验证 JWT 字符串
                .parseSignedClaims(token)
                // 获取 payload 部分
                .getPayload();
    }
}
