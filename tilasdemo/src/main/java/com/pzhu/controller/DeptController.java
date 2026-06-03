package com.pzhu.controller;

import com.pzhu.pojo.Dept;
import com.pzhu.pojo.Result;
import com.pzhu.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部门管理控制器 —— 处理部门相关的 HTTP 请求
 *
 * 【请求路径说明】
 * 类上的 @RequestMapping("/depts") 定义了基础路径，
 * 方法上的 @GetMapping/@PostMapping 等定义了具体路径。
 *
 * 完整的请求路径 = 类上的路径 + 方法上的路径
 * 例如：GET /depts → selectAll() 方法
 *      POST /depts → addDept() 方法
 *      GET /depts/1 → selectById(1) 方法（1是部门ID）
 *
 * 【@RestController 注解】
 * @Controller + @ResponseBody 的组合注解：
 * - @Controller：标记这是一个控制器
 * - @ResponseBody：方法的返回值直接转为 JSON 返回给前端
 *
 * 【@RequestMapping("/depts")】
 * 这个类中所有请求路径的前缀都是 /depts
 */
@RestController         // RESTful 控制器
@RequestMapping("/depts") // 请求路径前缀
public class DeptController {

    @Autowired  // 自动注入 DeptService（Spring 会自动创建实例并注入）
    DeptService deptService;

    /**
     * 查询所有部门
     * GET 请求 /depts
     *
     * 【@GetMapping】
     * 处理 HTTP GET 请求。GET 请求通常用于"获取数据"。
     * 等同于 @RequestMapping(value = "", method = RequestMethod.GET)
     *
     * @return 包含部门列表的 Result 对象
     */
    @GetMapping
    public Result selectAll() {
        // 调用 Service 层查询所有部门
        List<Dept> depts = deptService.selectAll();
        // 将部门列表封装为统一格式返回
        return Result.success(depts);
    }

    /**
     * 根据ID删除部门
     * DELETE 请求 /depts?id=1
     *
     * 【@DeleteMapping】
     * 处理 HTTP DELETE 请求。用于"删除数据"。
     *
     * 【@RequestParam 注解】
     * 从请求 URL 中获取参数的值。
     * 前端请求：DELETE /depts?id=1
     * 这里 Integer id 的值就是 1
     *
     * @param id 部门ID（通过请求参数传递）
     * @return 操作结果
     */
    @DeleteMapping
    public Result deleteById(@RequestParam Integer id) {
        deptService.deleteById(id);
        return Result.success("删除部门成功");
    }

    /**
     * 新增部门
     * POST 请求 /depts
     *
     * 【@PostMapping】
     * 处理 HTTP POST 请求。用于"新增数据"。
     *
     * 【@RequestBody 注解】
     * 将前端请求体中的 JSON 数据转换为 Java 对象。
     * 前端发送：{"name": "研发部"}
     * 后端接收：Dept 对象的 name 属性被赋值为 "研发部"
     *
     * @param dept 部门数据（由前端 JSON 自动转换而来）
     * @return 操作结果
     */
    @PostMapping
    public Result addDept(@RequestBody Dept dept) {
        deptService.addDept(dept);
        return Result.success("新增部门成功");
    }

    /**
     * 修改部门
     * PUT 请求 /depts
     *
     * 【@PutMapping】
     * 处理 HTTP PUT 请求。用于"修改数据"。
     *
     * @param dept 部门数据（包含 id 和修改后的 name）
     * @return 操作结果
     */
    @PutMapping
    public Result updateDept(@RequestBody Dept dept) {
        deptService.updateDept(dept);
        return Result.success("修改部门成功");
    }

    /**
     * 根据ID查询部门（用于编辑时回显数据）
     * GET 请求 /depts/1
     *
     * 【@PathVariable 注解】
     * 从 URL 路径中获取参数的值。
     * 请求路径 /depts/1 中的 "1" 会被提取出来赋值给 id 参数。
     * 注意：@GetMapping("/{id}") 中的 {id} 是路径变量，
     * @PathVariable Integer id 就是取这个变量的值。
     *
     * @param id 部门ID（从 URL 路径中获取）
     * @return 包含部门信息的 Result 对象
     */
    @GetMapping("/{id}")
    public Result selectById(@PathVariable Integer id) {
        Dept dept = deptService.selectById(id);
        return Result.success(dept);
    }
}
