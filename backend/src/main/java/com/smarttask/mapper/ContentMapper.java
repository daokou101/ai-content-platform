package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarttask.entity.Content;
import com.smarttask.vo.ContentVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 内容 Mapper 接口
 *
 * 继承 MyBatis-Plus 的 BaseMapper，自动获得基础的 CRUD 方法：
 *   - insert(Content)        → INSERT 一条记录
 *   - deleteById(Long)       → 按 ID 删除（逻辑删除，因为 @TableLogic 注解）
 *   - updateById(Content)    → 按 ID 更新
 *   - selectById(Long)       → 按 ID 查询
 *   - selectList(LambdaQueryWrapper) → 条件查询列表
 *   - selectPage(Page, LambdaQueryWrapper) → 分页查询
 *   - selectCount(LambdaQueryWrapper) → 条件计数
 *
 * 自定义方法：
 * 当 BaseMapper 提供的基础方法不够用时（比如需要多表 JOIN 查询），
 * 在这里定义方法，然后在 resources/mapper/ContentMapper.xml 中写 SQL。
 *
 * @Param : MyBatis 注解，用于给 SQL 参数命名。
 *   不加的话 MyBatis 无法识别参数名（Java 编译后参数名会变成 arg0, arg1）
 *
 * 使用到的位置：
 *   - ContentService.java → 调用各种 Mapper 方法进行数据库操作
 *   - ContentMapper.xml → 自定义 SQL 的实现
 */
public interface ContentMapper extends BaseMapper<Content> {

    /**
     * 分页查询内容列表（带关联信息）
     *
     * Page 参数是 MyBatis-Plus 的分页模型：
     *   必须放在第一个参数位置，MyBatis-Plus 的 PaginationInterceptor 会自动识别它
     *   并自动在 SQL 前后拼接 COUNT 查询和 LIMIT 语句。
     *
     * 和 BaseMapper.selectPage() 的区别：
     *   普通的 selectPage 只能查 Content 本身的字段，
     *   这个方法 LEFT JOIN 了分类表和用户表，可以拿到分类名称和创建者昵称。
     *
     * @param page         MyBatis-Plus 分页模型（new Page<>(pageNum, pageSize)），
     *                     MyBatis-Plus 会自动从 Page 中提取 pageNum 和 pageSize
     *                     并在 SQL 中追加 LIMIT (pageNum-1)*pageSize, pageSize
     *                     还会自动执行 COUNT 查询获取总条数
     * @param keyword      搜索关键词（模糊匹配标题和关键词字段），可为空字符串
     * @param templateType 模板类型筛选，可为空字符串
     * @param userId       当前登录用户ID，只查询该用户的内容
     * @return 内容 VO 列表，包含 categoryName(分类名) 和 createdByName(创建者昵称)
     */
    List<ContentVO> selectContentVOList(Page<ContentVO> page,
                                        @Param("keyword") String keyword,
                                        @Param("templateType") String templateType,
                                        @Param("userId") Long userId);

    /**
     * 查询单条内容详情（带关联信息）
     *
     * @param id 内容ID
     * @return 内容 VO，包含分类名和创建者昵称；不存在返回 null
     */
    ContentVO selectContentVODetail(@Param("id") Long id);

    /**
     * 统计用户今天生成了多少条内容
     *
     * 用于首页仪表盘的"今日生成"统计数据。
     * 条件：created_by = userId AND create_time >= 今天零点 AND deleted = 0
     *
     * @param userId 用户ID
     * @return 今日生成的内容数量
     */
    Long countTodayByUser(@Param("userId") Long userId);

    /**
     * 统计用户收藏了多少条内容
     *
     * 用于首页仪表盘的"收藏内容"统计数据。
     * 条件：created_by = userId AND favorite = 1 AND deleted = 0
     *
     * @param userId 用户ID
     * @return 收藏的内容数量
     */
    Long countFavoritesByUser(@Param("userId") Long userId);
}
