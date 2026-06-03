package com.pzhu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页查询结果类 —— 封装分页查询的返回数据
 *
 * 【什么是分页？】
 * 当数据量很大时（比如有1000个员工），一次查出来太多会卡顿。
 * 所以采用"分页"的方式：一次只查一页（比如10条），用户点击"下一页"再查后面的。
 *
 * 【泛型 <T>】
 * 表示这个类可以封装任意类型的分页数据。
 * PageResult<Emp> 表示员工的分页数据
 * PageResult<Dept> 表示部门的分页数据
 *
 * 【属性说明】
 * - total：总记录数（比如员工总共有 100 条数据）
 * - rows：当前页的数据列表（比如第1页的10条数据）
 */
@Data                     // 自动生成 getter/setter/toString
@NoArgsConstructor        // 无参构造
@AllArgsConstructor       // 全参构造
public class PageResult<T> {
    private Long total;   // 总记录数
    private List<T> rows; // 当前页的数据列表
}
