package com.pzhu.interceptor;

import com.pzhu.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器
 * <p>
 * 【什么是拦截器？】
 * 拦截器是 Spring MVC 提供的一种机制，可以在请求到达 Controller 之前或之后执行一些代码。
 * 就像"安检闸机"：每个请求进来都要先经过拦截器，符合条件才能放行。
 * <p>
 * 【为什么需要登录拦截器？】
 * 系统中的大部分接口（部门管理、员工管理等）都需要用户登录后才能访问。
 * 如果没有拦截器，任何人都可以随意调用这些接口，非常不安全。
 * <p>
 * 【工作流程】
 * 1. 用户登录成功 → 后端生成 JWT → 前端保存 JWT
 * 2. 前端每次请求时在 Header 中携带 JWT
 * 3. 拦截器在每个请求到达 Controller 前拦截 → 检查 JWT 是否有效
 * 4. 有效 → 放行（继续访问接口）；无效 → 返回 401 错误
 * <p>
 * 【@Component 注解】
 * 将当前类标记为 Spring 的一个组件，Spring 会自动创建它的实例并管理它。
 * 这样在 WebConfig 中就可以通过 @Autowired 注入这个拦截器了。
 */
@Slf4j // Lombok：生成日志对象
@Component // 标记为 Spring 组件
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * 在请求到达 Controller 之前执行（预处理）
     * <p>
     * 返回 true：放行（继续执行后续的拦截器和 Controller）
     * 返回 false：拦截（不会继续执行 Controller）
     *
     * @param request   HTTP 请求对象
     * @param response  HTTP 响应对象
     * @param handler   要执行的处理器（Controller 中的方法）
     * @return true=放行, false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 1. 从请求头中获取令牌（前端在请求头中放了一个叫 "token" 的字段）
        //    前端代码：axios.defaults.headers.common['token'] = token
        String token = request.getHeader("token");

        // 打印请求信息（方便调试）
        log.info("请求路径：{}，请求方式：{}，令牌：{}",
                request.getRequestURI(), request.getMethod(), token);

        // 2. 如果 token 不存在，说明用户未登录，返回 401 错误
        if (token == null || token.isEmpty()) {
            log.warn("令牌为空，请求被拒绝");
            // 设置响应状态码为 401（未授权）
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            // 设置响应的内容类型为 JSON
            response.setContentType("application/json;charset=utf-8");
            // 返回错误信息
            response.getWriter().write("{\"code\":0,\"msg\":\"未登录，请先登录\"}");
            return false; // 拦截请求
        }

        // 3. 令牌存在，验证令牌的有效性
        try {
            // 解析 JWT，如果解析成功说明令牌有效
            // 如果令牌被篡改或已过期，parseToken 会抛出异常
            JwtUtils.parseToken(token);
            log.info("令牌验证通过");
            return true; // 放行请求

        } catch (Exception e) {
            // 令牌无效（过期、被篡改等）
            log.warn("令牌无效：{}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write("{\"code\":0,\"msg\":\"登录已过期，请重新登录\"}");
            return false;
        }
    }
}
