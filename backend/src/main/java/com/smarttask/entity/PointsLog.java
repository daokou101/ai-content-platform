package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 积分变动日志实体类
 *
 * 记录用户每次积分变动：签到、做任务、充值、管理员调整等
 * 保证积分变动可追溯
 *
 * 属性说明：
 *   id          - 日志ID
 *   userId      - 用户ID
 *   points      - 变动的积分数（正数为增加，负数为扣减）
 *   type        - 变动类型：SIGN_IN(签到) TASK_DONE(完成任务) RECHARGE(充值) UPGRADE(升级消费) ADMIN_ADJUST(管理员调整)
 *   description - 变动描述，如"完成文章撰写任务，获得+50积分"
 *   operatorId  - 操作人ID（管理员调整时的管理员ID）
 *   createTime  - 变动时间
 */
@Data
@TableName("sys_points_log")
public class PointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Integer points;

    private String type;

    private String description;

    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
