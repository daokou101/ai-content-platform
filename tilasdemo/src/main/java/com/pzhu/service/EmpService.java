package com.pzhu.service;

import com.pzhu.pojo.Emp;
import com.pzhu.pojo.PageResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工管理业务层接口
 *
 * 【page 方法说明】
 * 分页查询涉及多个参数，所以方法签名比较长：
 * - page：当前页数
 * - pageSize：每页条数
 * - name / gender / begin / end：筛选条件
 *
 * 【countByJob 方法说明】
 * 用于"员工信息统计"图表，按职位分组统计人数。
 */
public interface EmpService {

    /**
     * 分页条件查询员工
     * @param page 当前页码
     * @param pageSize 每页条数
     * @param name 员工姓名（可选，模糊查询）
     * @param gender 性别（可选）
     * @param begin 入职开始日期（可选）
     * @param end 入职结束日期（可选）
     * @return 分页结果
     */
    PageResult<Emp> page(Integer page, Integer pageSize,
                         String name, Integer gender,
                         LocalDate begin, LocalDate end);

    /**
     * 按职位统计员工人数
     * @return List<Map>，每个 Map 包含 job 和 count 两个字段
     */
    List<Map<String, Object>> countByJob();
}
