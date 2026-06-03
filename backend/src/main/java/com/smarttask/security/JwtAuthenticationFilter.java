package com.smarttask.security;

import com.smarttask.common.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 *
 * OncePerRequestFilter: Spring Security 提供的过滤器基类，确保每个请求只被过滤一次
 *
 * 作用：
 *   每次请求到达时，从请求头中提取 JWT Token，解析出用户信息，
 *   将认证信息放入 SecurityContextHolder（Spring Security 的上下文持有者），
 *   这样后续的 Controller/Service 就能通过 SecurityContextHolder 获取当前用户信息
 *
 * 流程：
 *   1. 从 Authorization 请求头中取 Token
 *   2. 验证 Token 是否有效
 *   3. 从 Token 中解析用户信息
 *   4. 构建 UsernamePasswordAuthenticationToken 放入 SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 从请求头或查询参数中提取 JWT Token
        // 为什么要从查询参数提取？
        //   前端 Generate.vue 使用 EventSource 调用 SSE 端点（/api/ai/generate/sse），
        //   而 EventSource 无法设置 HTTP 请求头（包括 Authorization），
        //   所以 JWT token 通过 URL 查询参数传递：/api/ai/generate/sse?keywords=xxx&token=xxx
        //   这里需要兼容两种方式：
        //     1. 标准方式：从 Authorization: Bearer xxx 请求头提取（axios 请求）
        //     2. SSE 方式：从 URL 查询参数 token=xxx 提取（EventSource 请求）
        String token = extractToken(request);
        if (!StringUtils.hasText(token)) {
            // 如果请求头中没有 Token，尝试从查询参数中提取（兼容 SSE）
            token = request.getParameter("token");
        }

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            CustomUserDetails userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(username);

            /**
             * UsernamePasswordAuthenticationToken: Spring Security 的认证令牌
             * 三个参数: principal(用户信息), credentials(凭证), authorities(权限集合)
             * 调用带三个参数的构造方法会设置 setAuthenticated(true)
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities()
                    );

            /**
             * SecurityContextHolder: 线程绑定的安全上下文
             * 存储当前请求的认证信息，后续可以通过 SecurityContextHolder.getContext().getAuthentication() 获取
             * 请求结束后自动清除，避免内存泄漏
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取 Bearer Token
     * 标准格式: Authorization: Bearer xxxxx.yyyyy.zzzzz
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        /*
        hasText，stringutils的工具
        字符串 不是 null
        字符串 不是空串 ""
        字符串 不全是空格 / 制表符 / 换行
         */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // 截取字符串：从第7位开始，去掉前面的 "Bearer "
            // Bearer 一共6个字母 + 1个空格 = 7个字符
            return bearerToken.substring(7);
        }
        return null;
    }
}
