package com.smarttask.controller;

import com.smarttask.common.api.Result;
import com.smarttask.entity.Notification;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知控制器
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取未读通知数
     */
    @GetMapping("/unread-count")
    public Result<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(Map.of("count", notificationService.getUnreadCount(userDetails.getUserId())));
    }

    /**
     * 获取通知列表
     */
    @GetMapping
    public Result<List<Notification>> getNotifications(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(notificationService.getUserNotifications(userDetails.getUserId()));
    }

    /**
     * 标记单条通知为已读
     */
    @PutMapping("/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return Result.success();
    }

    /**
     * 全部标记为已读
     */
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(@AuthenticationPrincipal CustomUserDetails userDetails) {
        notificationService.markAllAsRead(userDetails.getUserId());
        return Result.success();
    }
}
