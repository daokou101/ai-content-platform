package com.aicreator.service;

import com.aicreator.common.api.ResultCode;
import com.aicreator.common.constant.CommonConstants;
import com.aicreator.common.exception.BusinessException;
import com.aicreator.common.utils.JwtUtils;
import com.aicreator.dto.LoginDTO;
import com.aicreator.entity.User;
import com.aicreator.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;

    public Map<String, Object> login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername()));
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.USERNAME_OR_PASSWORD_ERROR);
        }
        if (user.getStatus() != 0) {
            throw new BusinessException("账号已被禁用");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        redisTemplate.opsForValue().set(CommonConstants.REDIS_TOKEN_PREFIX + user.getId(), token, 24, TimeUnit.HOURS);
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname() != null ? user.getNickname() : "");
        result.put("avatar", user.getAvatar() != null ? user.getAvatar() : "");
        result.put("role", user.getRole());
        return result;
    }

    public void register(String username, String password) {
        long count = userMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username));
        if (count > 0) throw new BusinessException(ResultCode.USERNAME_EXISTS);
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(username);
        user.setRole(CommonConstants.ROLE_USER);
        user.setStatus(0);
        user.setAvatar(CommonConstants.DEFAULT_AVATAR);
        userMapper.insert(user);
    }
}
