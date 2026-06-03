package com.smarttask;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码哈希生成工具
 * 运行 main 方法即可生成 BCrypt 加密后的密码哈希
 */
public class PasswordEncoderUtil {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 生成 admin123 的哈希
        String adminHash = encoder.encode("admin123");
        System.out.println("admin / admin123 → " + adminHash);

        // 生成 user123 的哈希
        String userHash = encoder.encode("user123");
        System.out.println("user / user123   → " + userHash);
    }
}
