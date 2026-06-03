package com.aicreator.controller;

import com.aicreator.common.api.Result;
import com.aicreator.dto.LoginDTO;
import com.aicreator.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody LoginDTO dto) {
        authService.register(dto.getUsername(), dto.getPassword());
        return Result.success();
    }
}
