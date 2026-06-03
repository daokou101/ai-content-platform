package com.aicreator.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JwtUser {
    private Long userId;
    private String username;
    private String role;
}
