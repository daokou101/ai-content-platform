package com.smarttask.common.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * JwtUtils 单元测试
 *
 * 测试 JWT Token 的生成、解析和验证功能。
 * 使用 ReflectionTestUtils 注入私有字段（secret, expiration），
 * 不需要启动 Spring 容器。
 */
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 使用 ReflectionTestUtils 注入私有字段（替代 Spring 的 @Value）
        ReflectionTestUtils.setField(jwtUtils, "secret", "MyTestSecretKeyForJwtUnitTesting12345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "expiration", 3600000L); // 1 小时
    }

    @Test
    @DisplayName("生成 Token - 成功生成有效的 JWT")
    void generateToken_success() {
        String token = jwtUtils.generateToken(1L, "testuser", "NORMAL_USER");

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3, "JWT 应包含三部分（Header.Payload.Signature）");
    }

    @Test
    @DisplayName("从 Token 中提取用户 ID")
    void getUserIdFromToken_returnsCorrectId() {
        String token = jwtUtils.generateToken(42L, "testuser", "NORMAL_USER");

        Long userId = jwtUtils.getUserIdFromToken(token);

        assertEquals(42L, userId);
    }

    @Test
    @DisplayName("从 Token 中提取用户名")
    void getUsernameFromToken_returnsCorrectUsername() {
        String token = jwtUtils.generateToken(1L, "alice", "ADMIN");

        String username = jwtUtils.getUsernameFromToken(token);

        assertEquals("alice", username);
    }

    @Test
    @DisplayName("从 Token 中提取角色")
    void getRoleFromToken_returnsCorrectRole() {
        String token = jwtUtils.generateToken(1L, "admin", "SUPER_ADMIN");

        String role = jwtUtils.getRoleFromToken(token);

        assertEquals("SUPER_ADMIN", role);
    }

    @Test
    @DisplayName("验证 Token - 有效 Token 返回 true")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtils.generateToken(1L, "testuser", "NORMAL_USER");

        assertTrue(jwtUtils.validateToken(token));
    }

    @Test
    @DisplayName("验证 Token - 无效 Token 返回 false")
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtUtils.validateToken("invalid.token.here"));
        assertFalse(jwtUtils.validateToken(""));
        assertFalse(jwtUtils.validateToken(null));
    }

    @Test
    @DisplayName("验证 Token - 过期 Token 返回 false")
    void validateToken_expiredToken_returnsFalse() {
        // 生成一个过期时间很短（1 毫秒）的 Token
        JwtUtils shortLivedJwt = new JwtUtils();
        ReflectionTestUtils.setField(shortLivedJwt, "secret", "MyTestSecretKeyForJwtUnitTesting12345678901234567890");
        ReflectionTestUtils.setField(shortLivedJwt, "expiration", 1L); // 1 毫秒

        String token = shortLivedJwt.generateToken(1L, "testuser", "NORMAL_USER");

        // 等待 10 毫秒确保 Token 过期
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        assertFalse(shortLivedJwt.validateToken(token));
    }

    @Test
    @DisplayName("不同用户生成的 Token 不同")
    void generateToken_differentUsers_differentTokens() {
        String token1 = jwtUtils.generateToken(1L, "alice", "NORMAL_USER");
        String token2 = jwtUtils.generateToken(2L, "bob", "NORMAL_USER");

        assertNotEquals(token1, token2);
    }
}
