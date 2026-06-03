package com.smarttask.service;

import com.smarttask.common.api.ResultCode;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.common.exception.BusinessException;
import com.smarttask.common.utils.JwtUtils;
import com.smarttask.common.utils.RedisKeyBuilder;
import com.smarttask.common.utils.RequestContextUtils;
import com.smarttask.dto.LoginDTO;
import com.smarttask.dto.RegisterDTO;
import com.smarttask.entity.User;
import com.smarttask.mapper.UserMapper;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务
 * 处理用户登录、注册等认证相关业务
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final PointsService pointsService;

    /**
     * 用户登录
     *
     * @AuthenticationManager.authenticate(): Spring Security 的认证入口
     * 它会自动调用 CustomUserDetailsService.loadUserByUsername() 获取用户信息，
     * 然后自动进行密码比对，认证成功后返回 Authentication 对象
     * 我们从中提取用户信息，生成JWT Token返回给前端
     */
    public LoginVO login(LoginDTO loginDTO) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDTO.getUsername(), loginDTO.getPassword())
            );

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();

            // 更新登录信息
            user.setLastLoginIp(RequestContextUtils.getClientIp());
            user.setLastLoginTime(LocalDateTime.now());
            userMapper.updateById(user);

            // 生成 JWT Token
            String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());

            // 将 Token 存入 Redis，并设置过期时间（和 JWT 一致）
            // 这样做的好处：
            //   1. 方便管理员强制让某个用户下线（删除 Redis 中的 Token）
            //   2. 可以快速判断用户是否已登录
            String redisKey = RedisKeyBuilder.buildTokenKey(user.getId());
            redisTemplate.opsForValue().set(redisKey, token, 24, TimeUnit.HOURS);

            // 记录在线用户（使用 Redis Set 数据结构）
            redisTemplate.opsForSet().add(RedisKeyBuilder.buildOnlineKey(), String.valueOf(user.getId()));

            return LoginVO.builder()
                    .token(token)
                    .role(user.getRole())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .userId(user.getId())
                    .level(user.getLevel())
                    .points(user.getPoints())
                    .build();

        } catch (BadCredentialsException e) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
    }

    /**
     * 用户注册
     * 注册成功默认分配普通用户角色
     */
    public LoginVO register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, registerDTO.getUsername())
        );
        if (count > 0) {
            throw new BusinessException(ResultCode.USERNAME_EXISTS);
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 加密存储
        user.setNickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
        user.setEmail(registerDTO.getEmail());
        user.setAvatar(CommonConstants.DEFAULT_AVATAR);
        user.setRole(CommonConstants.ROLE_NORMAL_USER);
        user.setLevel(0);
        user.setPoints(0);
        user.setStatus(0);
        userMapper.insert(user);

        // 注册赠送 100 积分
        pointsService.addPoints(user.getId(), 100, CommonConstants.POINTS_SIGN_IN, "新用户注册赠送积分");

        // 注册完成后自动登录，返回 Token
        return login(new LoginDTO(registerDTO.getUsername(), registerDTO.getPassword()));
    }

    /**
     * 用户退出登录
     */
    public void logout(Long userId) {
        // 删除 Redis 中的 Token
        redisTemplate.delete(RedisKeyBuilder.buildTokenKey(userId));
        // 从在线用户集合中移除
        redisTemplate.opsForSet().remove(RedisKeyBuilder.buildOnlineKey(), String.valueOf(userId));
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(Long userId) {
        Boolean exists = redisTemplate.opsForSet().isMember(
                RedisKeyBuilder.buildOnlineKey(), String.valueOf(userId)
        );
        return Boolean.TRUE.equals(exists);
    }
}
