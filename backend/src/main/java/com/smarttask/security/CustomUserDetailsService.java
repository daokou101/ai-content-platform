package com.smarttask.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.entity.User;
import com.smarttask.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 自定义 UserDetailsService
 *
 * UserDetailsService: Spring Security 的核心接口，用于从数据库/缓存中加载用户信息
 * Spring Security 在登录时会自动调用 loadUserByUsername 方法获取用户信息
 * 并自动进行密码比对
 *
 * 这里从 MySQL 中查询用户，然后包装为 CustomUserDetails 返回
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;
    // 重写SpringSecurity的核心方法：根据用户名加载用户信息
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 调用MyBatis-Plus的Mapper，去数据库查询用户
        User user = userMapper.selectOne(
                // 2. MP的Lambda条件构造器（安全、不易写错）
                new LambdaQueryWrapper<User>()
                        /*
                        LambdaQueryWrapper<User>，MP 的 Lambda 版查询构造器，
                        专门针对 User 表（实体类）拼查询条件
                        eq = equals → 等于对应 SQL 的 WHERE username = ?
                        User::getUsername
                        这是 Java 8 方法引用，意思就是：User 实体类里的 username 字段
                        MP 会自动把它翻译成数据库的 username 列
                         */

                        // 3. 查询条件：数据库的 username = 用户登录输入的账号


                        .eq(User::getUsername, username)
        );

        if (user == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }

        return new CustomUserDetails(user);
    }
}
