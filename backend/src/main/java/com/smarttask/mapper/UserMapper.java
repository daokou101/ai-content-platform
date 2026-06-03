package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttask.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 用户 Mapper 接口
 *
 * BaseMapper<T>: MyBatis-Plus 提供的超级 Mapper，内置了 insert/deleteById/updateById/selectById/selectPage 等
 * 常用 CRUD 方法，继承后直接可用，无需写 XML
 * 当需要自定义 SQL 时，可以在接口中定义方法，然后在 resources/mapper/ 下写 XML
 */
public interface UserMapper extends BaseMapper<User> {

    /**
     * 更新用户积分（带原子性保证，比先查后改更安全）
     * @Update: MyBatis 直接注解 SQL，适用于简单 SQL 无需写 XML
     */
    @Update("UPDATE sys_user SET points = points + #{points} WHERE id = #{userId}")
    int updatePoints(@Param("userId") Long userId, @Param("points") int points);
}
