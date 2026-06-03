package com.pzhu.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pzhu.dao.EmpDao;
import com.pzhu.pojo.Emp;
import com.pzhu.pojo.PageResult;
import com.pzhu.service.EmpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工管理业务实现类
 *
 * 【PageHelper 分页原理】
 * PageHelper 是一个 MyBatis 的分页插件。使用方式很简单：
 * 1. 在查询前调用 PageHelper.startPage(page, pageSize)
 * 2. 接着执行查询语句
 * 3. PageHelper 会自动在 SQL 后面拼接 LIMIT 语句实现分页
 * 4. 查询结果会被自动封装成 Page 对象，里面包含 total（总条数）和 result（数据列表）
 *
 * 【分页示例】
 * 假设 page=2, pageSize=10
 * PageHelper 会在 SQL 后面自动加上：LIMIT 10, 10
 * 意思是从第10条开始查10条，也就是第2页的数据。
 */
@Service // 标记为 Spring 业务层组件
public class EmpServiceImpl implements EmpService {

    @Autowired // 自动注入 EmpDao
    private EmpDao empDao;

    @Override
    public PageResult<Emp> page(Integer page, Integer pageSize,
                                String name, Integer gender,
                                LocalDate begin, LocalDate end) {
        // 1. 设置分页参数（告诉 PageHelper 我要查第几页、每页几条）
        PageHelper.startPage(page, pageSize);

        // 2. 执行查询（PageHelper 会自动在 SQL 上加 LIMIT）
        Page<Emp> empPage = empDao.page(name, gender, begin, end);

        // 3. 封装分页结果（总条数 + 当前页数据）
        return new PageResult<>(empPage.getTotal(), empPage.getResult());
    }

    @Override
    public List<Map<String, Object>> countByJob() {
        // 按职位分组统计
        return empDao.countByJob();
    }
}
