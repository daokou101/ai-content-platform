package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttask.entity.ContentCategory;

/**
 * 内容分类 Mapper 接口
 *
 * 直接使用 BaseMapper 提供的方法即可满足需求，无需自定义 SQL。
 *
 * 提供的基础方法（在 ContentCategoryService 中调用）：
 *   - insert(ContentCategory)     → 新增分类
 *   - deleteById(Long)            → 删除分类（逻辑删除）
 *   - selectList(LambdaQueryWrapper) → 查询所有分类
 *
 * 为什么不写 XML？
 *   因为所有的操作都是单表 CRUD，没有复杂的 JOIN 或特殊 SQL，
 *   BaseMapper 自带的方法完全够用。
 *
 * 使用到的位置：
 *   - ContentService.java → 通过此 Mapper 操作分类表
 */
public interface ContentCategoryMapper extends BaseMapper<ContentCategory> {
}
