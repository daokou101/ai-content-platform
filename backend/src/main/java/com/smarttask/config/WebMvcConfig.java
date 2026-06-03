package com.smarttask.config;

import com.smarttask.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 *
 * WebMvcConfigurer: Spring MVC 的配置接口
 * 可以自定义拦截器、静态资源路径、跨域CORS等
 *
 * 这里注册了限流拦截器，对所有 /api/ 开头的请求进行限流
 */
@Configuration
@RequiredArgsConstructor
//Lombok 注解：为所有 final 成员变量生成构造方法（实现依赖注入，替代@Autowired）
public class WebMvcConfig implements WebMvcConfigurer {
    // 注入 自定义的【限流拦截器】（真正做限流逻辑的类）
    private final RateLimitInterceptor rateLimitInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitInterceptor)// 把限流拦截器注册到Spring
                .addPathPatterns("/api/**")  // 拦截所有 API 请求
                .excludePathPatterns("/api/auth/login", "/api/auth/register"); // 登录注册不限制
    }
}
