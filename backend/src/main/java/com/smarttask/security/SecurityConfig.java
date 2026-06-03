package com.smarttask.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 核心配置类
 *
 * @EnableWebSecurity: 启用 Spring Security 的 Web 安全功能
 * @EnableMethodSecurity: 启用方法级别的安全注解（如 @PreAuthorize），替代过时的 @EnableGlobalMethodSecurity
 *
 * SecurityFilterChain: Spring Security 3.x 引入的配置方式（替代继承 WebSecurityConfigurerAdapter）
 * 通过建造者模式链式配置安全规则
 *
 * 配置要点：
 *   1. 全部放行登录/注册/静态资源等接口
 *   2. 其他请求必须认证
 *   3. 无状态模式（不用 Session，用 JWT）
 *   4. 自定义 JWT 过滤器放在 UsernamePasswordAuthenticationFilter 之前
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity// 开启方法级权限控制（比如@PreAuthorize("hasRole('admin')")）
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // 白名单：不需要登录即可访问的路径
    private static final String[] WHITE_LIST = {
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/ws/**",           // WebSocket 连接
            "/v3/api-docs/**",  // Swagger 文档
            "/swagger-ui/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（跨站请求伪造），因为 JWT 天然免疫 CSRF 攻击
                // CSRF 攻击原理：利用浏览器自动携带 Cookie 的特性，而 JWT 存在请求头中，不会自动携带
                .csrf(csrf -> csrf.disable())

                // 设置无状态会话（STATELESS），不用 Session，每个请求独立认证
                // 因为我们是前后端分离 + JWT，不需要服务端 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()   // 白名单放行
                        .anyRequest().authenticated()              // 其余全部需认证
                )

                // 将 JWT 过滤器添加到 UsernamePasswordAuthenticationFilter 之前
                // Spring Security 先经过 JWT 过滤器解析 Token，再执行后续认证流程
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationManager: Spring Security 的认证管理器
     * 负责处理认证请求（调用 UserDetailsService 获取用户信息 + 密码比对）
     * 在登录接口中需要注入此 Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * PasswordEncoder: 密码编码器
     * BCryptPasswordEncoder: 使用 BCrypt 强哈希算法加密密码
     * - 每次加密结果不同（内置 salt），即使相同密码加密结果也不同
     * - 不可逆，无法从密文反推明文
     * - 自动处理 salt 的存储和验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
