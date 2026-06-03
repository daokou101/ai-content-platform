package com.pzhu.controller;

import com.pzhu.pojo.Result;
import com.pzhu.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录控制器
 * <p>
 * 【@RestController 注解】
 * = @Controller + @ResponseBody 的组合。
 * - @Controller：标记这是一个控制器类，能接收 HTTP 请求
 * - @ResponseBody：方法的返回值会直接写入 HTTP 响应体（而不是跳转页面）
 * 也就是说，这个类中所有方法的返回值会自动转换为 JSON 格式返回给前端。
 * <p>
 * 【@RequestMapping("/login") 注解】
 * 设置请求路径的前缀，类中所有方法的 URL 都以 /login 开头。
 * 比如 login() 方法的完整路径就是 /login（因为类上已经写了 /login）。
 * <p>
 * 【@Slf4j 注解】
 * Lombok 提供的日志注解，会自动生成一个 log 对象，可以直接用 log.info() 打印日志。
 * 比如：log.info("用户登录成功");
 * <p>
 * 【登录流程】
 * 前端 POST /login → 后端接收用户名密码 → 查询数据库验证 → 生成 JWT → 返回给前端
 */
@Slf4j // Lombok：自动创建日志对象
@RestController // 标记为 RESTful 控制器
@RequestMapping("/login") // 请求路径前缀
public class LoginController {

    @Autowired // 注入 JdbcTemplate，用于执行 SQL 查询
    private JdbcTemplate jdbcTemplate;

    /**
     * 用户登录
     * <p>
     * 【@PostMapping 注解】
     * 处理 HTTP POST 请求。POST 通常用于"提交数据"（登录时提交用户名密码）。
     *
     * 【@RequestBody 注解】
     * 将前端传来的 JSON 数据自动转换为 Java 的 Map 对象。
     * 前端传的 JSON：{"username": "admin", "password": "123456"}
     * 后端接收：Map 中就有 key=username, value=admin
     *
     * 【为什么用 Map 而不用实体类？】
     * 因为登录只需要用户名和密码两个字段，专门建一个实体类太麻烦，用 Map 更轻量。
     *
     * @param loginForm 登录表单数据（包含 username 和 password）
     * @return Result 对象（成功时包含 JWT 令牌）
     */
    @PostMapping // 处理 POST 请求
    public Result login(@RequestBody Map<String, String> loginForm) {
        // 1. 从前端发来的 JSON 中取出用户名和密码
        String username = loginForm.get("username");
        String password = loginForm.get("password");

        // 记录日志：有人尝试登录（方便排查问题）
        log.info("用户尝试登录：{}", username);

        // 2. 参数校验：如果用户名为空或密码为空，直接返回错误
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return Result.error("用户名或密码不能为空");
        }

        // 3. 查询数据库，验证用户名密码是否正确
        try {
            // 使用 JdbcTemplate 查询 user 表，看用户名和密码是否匹配
            // queryForObject() 会返回查询结果，如果没查到会抛异常
            Map<String, Object> user = jdbcTemplate.queryForMap(
                    "SELECT id, username, name FROM user WHERE username = ? AND password = ?",
                    username, password
            );

            // 4. 验证通过，生成 JWT 令牌
            //    JWT 中存放用户的 id、username 等信息
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.get("id"));
            claims.put("username", user.get("username"));
            claims.put("name", user.get("name"));

            // 调用工具类生成令牌
            String token = JwtUtils.generateToken(claims);

            // 记录成功日志
            log.info("用户 {} 登录成功", username);

            // 5. 将令牌封装到 Result 中返回给前端
            //    {code: 1, msg: "success", data: {token: "xxx...", name: "张三"}}
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("name", user.get("name"));

            return Result.success(data);

        } catch (EmptyResultDataAccessException e) {
            // queryForMap 查不到数据时会抛出这个异常
            // 说明用户名或密码错误
            log.warn("用户 {} 登录失败：用户名或密码错误", username);
            return Result.error("用户名或密码错误");
        }
    }
}
