package com.pzhu.service.impl;

import com.pzhu.dao.DeptDao;
import com.pzhu.pojo.Dept;
import com.pzhu.service.DeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门管理业务实现类
 *
 * 【@Service 注解】
 * 标记为 Spring 的"业务层"组件。
 * Spring 会自动创建这个类的实例（Bean），并放入 IOC 容器中管理。
 * 当 DeptController 中使用 @Autowired 注入 DeptService 时，
 * Spring 会把这里创建的实例注入进去。
 *
 * 【业务逻辑说明】
 * 这里的业务逻辑比较简单，就是对 DAO 层的"包装"，
 * 在调用 DAO 方法前后加上一些额外的处理。
 * 比如 addDept() 中在插入数据前设置了 createTime 和 updateTime。
 */
@Service // 标记为 Spring 业务层组件
public class DeptServiceImpl implements DeptService {

    @Autowired // 自动注入 DeptDao（Spring 会自动创建 DeptDao 的实例）
    DeptDao deptDao;

    @Override
    public List<Dept> selectAll() {
        // 直接调用 DAO 层查询所有部门
        return deptDao.selectAll();
    }

    @Override
    public void deleteById(Integer id) {
        // 调用 DAO 层删除指定 ID 的部门
        deptDao.deleteById(id);
    }

    @Override
    public void addDept(Dept dept) {
        // 设置创建时间和修改时间（系统当前时间）
        dept.setCreateTime(LocalDateTime.now());
        dept.setUpdateTime(LocalDateTime.now());
        // 调用 DAO 层插入数据
        deptDao.insert(dept);
    }

    @Override
    public void updateDept(Dept dept) {
        // 设置修改时间（创建时间不变）
        dept.setUpdateTime(LocalDateTime.now());
        // 调用 DAO 层更新数据
        deptDao.updateById(dept);
    }

    @Override
    public Dept selectById(Integer id) {
        // 调用 DAO 层根据 ID 查询部门
        return deptDao.selectById(id);
    }
}
