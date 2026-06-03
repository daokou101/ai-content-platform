package com.smarttask.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smarttask.common.api.Result;
import com.smarttask.dto.UserUpdateDTO;
import com.smarttask.service.UserService;
import com.smarttask.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员控制器
 *
 * @PreAuthorize: Spring Security 方法级别权限控制注解
 *   hasRole('ADMIN'): 要求用户拥有 ADMIN 角色
 *   这里的 ADMIN 对应 SecurityConfig 中设置的 ROLE_ADMIN 权限
 *   实际上 hasRole('ADMIN') 会检查 GrantedAuthority 中是否有 ROLE_ADMIN
 *
 * 只有 ADMIN 和 SUPER_ADMIN 角色的用户可以访问此控制器中的接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    /**
     * 分页查询用户列表
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<IPage<UserInfoVO>> getUserList(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.getUserPage(pageNum, pageSize, keyword));
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<UserInfoVO> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    /**
     * 更新用户信息（包括权限等级）
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> updateUser(@PathVariable Long id, @RequestBody UserUpdateDTO updateDTO) {
        userService.updateUser(id, updateDTO);
        return Result.success();
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/users/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        userService.resetPassword(id, body.get("password"));
        return Result.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 只有超级管理员才能删除用户
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
}
