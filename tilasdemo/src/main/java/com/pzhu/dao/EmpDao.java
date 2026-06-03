package com.pzhu.dao;

import com.github.pagehelper.Page;
import com.pzhu.pojo.Emp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 员工管理数据访问层（DAO）
 *
 * 【为什么 EmpDao 用 XML 而 DeptDao 用注解？】
 * 因为员工查询涉及多表关联（LEFT JOIN）、动态 SQL（<if> 条件判断）、
 * 排序等复杂操作，用注解写 SQL 会变得很长且不易读。
 * 所以员工查询使用 XML 文件（resources/mapper/EmpDao.xml）来写 SQL。
 *
 * 【Page<Emp> 返回类型】
 * Page 是 PageHelper 提供的分页对象，继承自 ArrayList，
 * 它除了包含查询到的数据列表外，还额外存储了 total（总条数）等分页信息。
 *
 * 【@Param 注解】
 * 当方法有多个参数时，需要用 @Param 给每个参数取一个名字，
 * 这样在 XML 中就可以用 #{name}、#{gender} 等方式引用参数。
 */
@Mapper // 标记为 MyBatis 映射器接口
public interface EmpDao {

    /**
     * 分页条件查询员工
     * SQL 写在 resources/mapper/EmpDao.xml 中
     *
     * @param name   员工姓名（模糊查询）
     * @param gender 性别
     * @param begin  入职开始日期
     * @param end    入职结束日期
     * @return 分页结果（包含数据列表 + 总条数）
     */
    Page<Emp> page(@Param("name") String name,
                   @Param("gender") Integer gender,
                   @Param("begin") LocalDate begin,
                   @Param("end") LocalDate end);

    /**
     * 按职位统计员工人数
     * SQL 写在 resources/mapper/EmpDao.xml 中
     *
     * @return List<Map>，每行包含 job（职位编号）和 count（人数）
     */
    List<Map<String, Object>> countByJob();
}
