package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @TableName: MyBatis-Plus 注解，指定数据库表名
 *   如果不指定，默认使用类名下划线风格（User → user）
 * @TableId: 主键注解，type = IdType.AUTO 表示数据库自增
 * @TableField: 非主键字段映射，fill = FieldFill.INSERT 表示插入时自动填充
 * @TableLogic: 逻辑删除注解，查询时自动拼接 WHERE deleted = 0
 *
 * 属性说明：
 *   id          - 用户ID（主键自增）
 *   username    - 登录用户名
 *   password    - 登录密码（加密存储）
 *   nickname    - 用户昵称（显示用）
 *   email       - 邮箱
 *   avatar      - 头像URL
 *   phone       - 手机号
 *   role        - 角色编码（SUPER_ADMIN/ADMIN/VIP_USER/NORMAL_USER）
 *   points      - 当前积分
 *   status      - 状态：0正常 1禁用
 *   level       - 权限等级（数值越大权限越高）：0普通用户 1VIP用户 2管理员 3超级管理员
 *   lastLoginIp - 最后登录IP
 *   lastLoginTime - 最后登录时间
 *   deleted     - 逻辑删除标记（0未删 1已删）
 *   createTime  - 创建时间
 *   updateTime  - 更新时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField
    private String username;

    private String password;

    private String nickname;

    private String email;

    private String avatar;

    private String phone;

    private String role;

    private Integer points;

    private Integer status;

    private Integer level;

    private String lastLoginIp;

    private LocalDateTime lastLoginTime;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
