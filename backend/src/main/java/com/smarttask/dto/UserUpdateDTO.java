package com.smarttask.dto;

import lombok.Data;

/**
 * 用户信息更新DTO（管理员使用）
 */
@Data
public class UserUpdateDTO {

    private String nickname;

    private String email;

    private String phone;

    private String avatar;

    private Integer status;

    private Integer level;

    private String role;
}
