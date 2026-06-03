package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知消息实体类
 *
 * 系统内部通知，通过 WebSocket 实时推送给用户
 *
 * 属性说明：
 *   id          - 通知ID
 *   userId      - 接收通知的用户ID
 *   title       - 通知标题
 *   content     - 通知内容（支持简单HTML）
 *   type        - 通知类型：TASK_ASSIGN(任务分配) TASK_REVIEW(任务审核) SYSTEM(系统通知) UPGRADE(权限变更)
 *   isRead      - 是否已读：0未读 1已读
 *   relatedId   - 关联的业务ID（如任务ID）
 *   createTime  - 创建时间
 */
@Data
@TableName("sys_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String content;

    private String type;

    private Integer isRead;

    private Long relatedId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
