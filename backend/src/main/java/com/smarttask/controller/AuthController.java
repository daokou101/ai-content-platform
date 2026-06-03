package com.smarttask.controller;

import com.smarttask.common.api.Result;
import com.smarttask.dto.LoginDTO;
import com.smarttask.dto.RegisterDTO;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.service.AuthService;
import com.smarttask.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
@SuppressWarnings("all")
/**
 * 认证控制器
 *
 * @RestController: @Controller + @ResponseBody 的组合注解
 *   类中所有方法返回值直接作为 HTTP 响应体（JSON格式），不走视图解析器
 * @RequestMapping("/api/auth"): 映射该控制器的请求路径前缀
 * @Valid: Jakarta Validation 注解，触发 DTO 中的参数校验（如 @NotBlank）
 * @AuthenticationPrincipal: Spring Security 注解，直接从 SecurityContext 中获取当前认证用户
 *   等价于 SecurityContextHolder.getContext().getAuthentication().getPrincipal()
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getUserId());
        return Result.success();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public Result<LoginVO> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        LoginVO loginVO = LoginVO.builder()
                .userId(userDetails.getUserId())
                .username(userDetails.getUsername())
                .nickname(userDetails.getUser().getNickname())
                .avatar(userDetails.getUser().getAvatar())
                .role(userDetails.getRole())
                .level(userDetails.getLevel())
                .points(userDetails.getUser().getPoints())
                .build();
        return Result.success(loginVO);
    }
}
