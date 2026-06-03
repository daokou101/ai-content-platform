package com.smarttask.security;

import com.smarttask.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * 自定义 UserDetails 实现
 *
 * UserDetails: Spring Security 定义的用户信息接口
 * Spring Security 通过此接口获取用户的认证信息和权限
 *
 * 将我们项目的 User 对象适配为 Spring Security 能识别的用户对象
 * 这种适配器模式（Adapter Pattern）让我们既保留了自己的 User 实体，
 * 又能融入 Spring Security 的认证体系
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }


    /**
     * 返回用户权限集合
     * 将用户的角色编码转换为 Spring Security 的 GrantedAuthority
     * 格式: ROLE_ADMIN, ROLE_NORMAL_USER
     */


    /**
     * 重写 Spring Security 的核心方法：获取用户的【权限/角色集合】
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回一个只有1个元素的固定列表（单角色用户）
        return Collections.singletonList(
                // Spring Security 标准的权限对象：封装用户角色
                new SimpleGrantedAuthority("ROLE_" + user.getRole())
        );
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 账号是否未过期（我们暂不实现过期机制，始终返回true）
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账号是否未锁定（status = 0 表示正常）
     */
    @Override
    public boolean isAccountNonLocked() {
        return user.getStatus() == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 账号是否启用
     */
    @Override
    public boolean isEnabled() {
        return user.getStatus() == 0;
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRole() {
        return user.getRole();
    }

    public Integer getLevel() {
        return user.getLevel();
    }
}
