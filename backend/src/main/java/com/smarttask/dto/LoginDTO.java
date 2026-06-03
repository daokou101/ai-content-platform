package com.smarttask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * Data Transfer Object: 专门用于客户端和服务端之间的数据传输
 * 和 Entity 分离，避免将数据库结构暴露给前端
 *
 * @NotBlank: Jakarta Validation 注解，校验字符串不能为 null 且不能是空字符串
 * 配合 @Valid 或 @Validated 使用，自动校验参数合法性
 */
@Data
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
