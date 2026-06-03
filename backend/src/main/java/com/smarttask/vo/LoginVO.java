package com.smarttask.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录成功后返回给前端的视图对象
 *
 * View Object: 专门用于返回给前端的数据封装
 * 不同于 Entity（数据库映射），VO 可以自由组合多个来源的数据
 *
 * @Builder: Lombok 建造者模式注解，可以用 LoginVO.builder().token("xxx").userInfo(yyy).build() 方式创建对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;       // JWT Token
    private String username;    // 用户名
    private String role;        // 用户角色
    private String nickname;    // 用户昵称
    private String avatar;      // 头像URL
    private Long userId;        // 用户ID
    private Integer level;      // 权限等级
    private Integer points;     // 积分
}
