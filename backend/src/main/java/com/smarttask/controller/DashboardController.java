package com.smarttask.controller;

import com.smarttask.common.api.Result;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.service.DashboardService;
import com.smarttask.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 仪表盘控制器
 *
 * 提供首页仪表盘的聚合数据
 * 不同角色看到的数据范围不同
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public Result<DashboardVO> getDashboard(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(
                dashboardService.getDashboard(userDetails.getUserId(), userDetails.getRole())
        );
    }
}
