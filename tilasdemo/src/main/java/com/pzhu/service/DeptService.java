package com.pzhu.service;

import com.pzhu.pojo.Dept;

import java.util.List;

/**
 * 部门管理业务层接口
 *
 * 【什么是 Service 接口？】
 * 接口定义了"做什么"，实现类（DeptServiceImpl）定义了"怎么做"。
 * 这样设计的好处是：
 * 1. 方便替换实现类（比如以后想换一种实现方式，只需要新增一个实现类）
 * 2. 面向接口编程，代码更灵活、耦合度更低
 *
 * 【业务层 vs 控制层 vs 数据层】
 * - Controller（控制层）：负责接收请求、返回响应
 * - Service（业务层）：负责业务逻辑处理
 * - DAO（数据层）：负责数据库操作
 *
 * 调用链：前端 → Controller → Service → DAO → 数据库
 */
public interface DeptService {

    /** 查询所有部门 */
    List<Dept> selectAll();

    /** 根据ID删除部门 */
    void deleteById(Integer id);

    /** 新增部门 */
    void addDept(Dept dept);

    /** 修改部门 */
    void updateDept(Dept dept);

    /** 根据ID查询部门 */
    Dept selectById(Integer id);
}
