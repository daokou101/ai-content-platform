package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttask.entity.AiModel;

/**
 * AI 模型 Mapper 接口
 *
 * 提供模型的数据库操作。
 * 模型数据在 init.sql 中初始化。
 *
 * 提供的基础方法：
 *   - selectList(LambdaQueryWrapper) → 查询所有可用模型（按 sortOrder 排序）
 *
 * 使用到的位置：
 *   - AiController.java → 查询所有模型列表返回给前端
 */
public interface AiModelMapper extends BaseMapper<AiModel> {
}
