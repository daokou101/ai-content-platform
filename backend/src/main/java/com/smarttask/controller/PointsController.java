package com.smarttask.controller;

import com.smarttask.common.api.Result;
import com.smarttask.entity.PointsLog;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 积分控制器
 *
 * 用户积分相关操作：签到、充值、查看积分历史
 * "充值"只是模拟逻辑，实际上就是增加积分，不涉及真实支付
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    /**
     * 每日签到
     */
    @PostMapping("/sign-in")
    public Result<Map<String, Object>> signIn(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long userId = userDetails.getUserId();

        if (pointsService.hasSignedInToday(userId)) {
            return Result.failed("今天已经签到过了，明天再来吧！");
        }

        pointsService.signIn(userId);
        Integer currentPoints = pointsService.getUserPoints(userId);

        return Result.success(Map.of(
                "points", 10,
                "totalPoints", currentPoints,
                "message", "签到成功！获得10积分"
        ));
    }

    /**
     * 模拟充值（增加积分）
     */
    @PostMapping("/recharge")
    public Result<Map<String, Object>> recharge(@RequestBody Map<String, Integer> body,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Integer amount = body.get("amount");
        if (amount == null || amount <= 0) {
            return Result.failed("充值金额必须大于0");
        }

        pointsService.recharge(userDetails.getUserId(), amount);
        Integer currentPoints = pointsService.getUserPoints(userDetails.getUserId());

        return Result.success(Map.of(
                "amount", amount,
                "earnedPoints", amount * 10,
                "totalPoints", currentPoints,
                "message", "充值成功！获得 " + (amount * 10) + " 积分"
        ));
    }

    /**
     * 获取积分变动历史
     */
    @GetMapping("/logs")
    public Result<List<PointsLog>> getPointsLogs(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(pointsService.getPointsLogs(userDetails.getUserId()));
    }

    /**
     * 获取当前积分
     */
    @GetMapping("/balance")
    public Result<Map<String, Object>> getBalance(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(Map.of(
                "points", pointsService.getUserPoints(userDetails.getUserId())
        ));
    }
}
