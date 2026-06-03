package com.smarttask.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 * 返回给前端的用户信息（不包含密码等敏感字段）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    private Long id;
    private String username;
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
    private LocalDateTime createTime;
}
