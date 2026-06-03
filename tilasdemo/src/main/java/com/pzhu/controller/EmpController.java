package com.pzhu.controller;

import com.pzhu.pojo.Emp;
import com.pzhu.pojo.PageResult;
import com.pzhu.pojo.Result;
import com.pzhu.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工管理控制器 —— 处理员工相关的 HTTP 请求
 *
 * 【请求路径说明】
 * 类上写 @RequestMapping("/emps")，所以所有接口都以 /emps 开头
 * - GET /emps → 分页查询员工
 * - GET /emps/stats/job → 按职位统计员工
 *
 * 【日期格式化说明】
 * 前端传的日期是字符串（"2024-01-15"），
 * 后端需要用 LocalDate 接收，并通过 @DateTimeFormat 指定日期格式。
 * 否则 Spring 不知道如何把字符串转换成日期对象。
 */
@RestController         // RESTful 控制器
@RequestMapping("/emps") // 请求路径前缀
public class EmpController {

    @Autowired // 自动注入 EmpService
    private EmpService empService;

    /**
     * 分页查询员工列表（支持条件筛选）
     * GET 请求 /emps?page=1&pageSize=10&name=&gender=&begin=&end=
     *
     * 【@RequestParam 注解的 defaultValue】
     * defaultValue = "1" 表示如果前端没有传 page 参数，默认就是第1页
     *
     * 【@DateTimeFormat 注解】
     * 指定日期字符串的格式，将 "2024-01-15" 这样的字符串转换成 LocalDate 对象
     *
     * @param page     当前页码（默认1）
     * @param pageSize 每页条数（默认10）
     * @param name     员工姓名（可选，用于模糊搜索）
     * @param gender   性别（可选）
     * @param begin    入职开始日期（可选）
     * @param end      入职结束日期（可选）
     * @return 包含分页结果的 Result 对象
     */
    @GetMapping
    public Result page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            String name,
            Integer gender,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        // 调用 Service 层进行分页查询
        PageResult<Emp> pageResult = empService.page(page, pageSize, name, gender, begin, end);
        return Result.success(pageResult);
    }

    /**
     * 按职位统计员工人数（用于 ECharts 图表展示）
     * GET 请求 /emps/stats/job
     *
     * @return 包含统计数据的 Result 对象
     *         数据格式：[{job: 1, count: 5}, {job: 2, count: 3}, ...]
     */
    @GetMapping("/stats/job")
    public Result countByJob() {
        List<Map<String, Object>> stats = empService.countByJob();
        return Result.success(stats);
    }
}
