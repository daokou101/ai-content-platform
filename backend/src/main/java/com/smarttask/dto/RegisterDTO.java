package com.smarttask.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 *
 * @Size: Jakarta Validation 注解，限制字符串长度范围
 */
@Data
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度在3-30之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 40, message = "密码长度在6-40之间")
    private String password;

    private String nickname;

    private String email;
}
