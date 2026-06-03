package com.smarttask.service;

import com.smarttask.common.api.ResultCode;
import com.smarttask.common.exception.BusinessException;
import com.smarttask.common.utils.JwtUtils;
import com.smarttask.dto.LoginDTO;
import com.smarttask.dto.RegisterDTO;
import com.smarttask.entity.User;
import com.smarttask.mapper.UserMapper;
import com.smarttask.vo.LoginVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * AuthService 单元测试
 *
 * 测试用户注册和登录功能。
 * 使用 Mockito 模拟 AuthenticationManager、UserMapper、JwtUtils 等依赖。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private PointsService pointsService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setUsername("testuser");
        sampleUser.setPassword("encodedPassword");
        sampleUser.setRole("NORMAL_USER");
        sampleUser.setNickname("测试用户");
        sampleUser.setPoints(100);

        loginDTO = new LoginDTO("testuser", "password123");
    }

    // ==================== 登录 ====================

    @Test
    @DisplayName("登录 - 成功登录返回 LoginVO")
    void login_success() {
        Authentication authentication = mock(Authentication.class);
        com.smarttask.security.CustomUserDetails userDetails =
                new com.smarttask.security.CustomUserDetails(sampleUser);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-jwt-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(mock());

        LoginVO result = authService.login(loginDTO);

        assertNotNull(result);
        assertEquals("test-jwt-token", result.getToken());
        assertEquals("NORMAL_USER", result.getRole());
        assertEquals("测试用户", result.getNickname());
        assertEquals(1L, result.getUserId());
    }

    @Test
    @DisplayName("登录 - 密码错误时抛出 BusinessException")
    void login_wrongPassword_throwsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("密码错误"));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.login(loginDTO));

        assertEquals(ResultCode.USERNAME_OR_PASSWORD_ERROR.getCode(), exception.getCode());
    }

    // ==================== 注册 ====================

    @Test
    @DisplayName("注册 - 成功创建用户并返回 LoginVO")
    void register_success() {
        // 1. 检查用户名不存在
        when(userMapper.selectCount(any())).thenReturn(0L);
        // 2. 加密密码
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        // 3. 插入用户 — 使用 doAnswer 模拟自增 ID
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        });
        // 4. 登录相关 mock
        Authentication authentication = mock(Authentication.class);
        com.smarttask.security.CustomUserDetails userDetails =
                new com.smarttask.security.CustomUserDetails(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString())).thenReturn("test-jwt-token");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(mock());

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("newuser");
        registerDTO.setPassword("password123");

        LoginVO result = authService.register(registerDTO);

        assertNotNull(result);
        // 验证注册赠送积分被调用
        verify(pointsService, times(1)).addPoints(anyLong(), eq(100), anyString(), anyString());
    }

    @Test
    @DisplayName("注册 - 用户名已存在时抛出异常")
    void register_usernameExists_throwsException() {
        when(userMapper.selectCount(any())).thenReturn(1L); // 用户名已存在

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("existinguser");
        registerDTO.setPassword("password123");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> authService.register(registerDTO));

        assertEquals(ResultCode.USERNAME_EXISTS.getCode(), exception.getCode());
        // 验证 insert 没有被调用
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    @DisplayName("注册 - 密码长度不足时加密仍正常工作")
    void register_shortPassword_encoded() {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedShortPwd");
        when(userMapper.insert(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return 1;
        });
        Authentication authentication = mock(Authentication.class);
        com.smarttask.security.CustomUserDetails userDetails =
                new com.smarttask.security.CustomUserDetails(sampleUser);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateToken(anyLong(), anyString(), anyString())).thenReturn("jwt");
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(mock());

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("user2");
        registerDTO.setPassword("123");

        authService.register(registerDTO);

        verify(passwordEncoder).encode("123");
    }
}
