package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttask.entity.AiTemplate;

/**
 * AI 模板 Mapper 接口
 *
 * 提供模板的数据库操作。
 * 模板数据在 init.sql 中初始化，运行期基本不修改。
 *
 * 提供的基础方法：
 *   - selectList(LambdaQueryWrapper) → 查询所有模板（按 sortOrder 排序）
 *   - selectOne(LambdaQueryWrapper)  → 按 type 查询单个模板（用于 AiService 查找 systemPrompt）
 *
 * 使用到的位置：
 *   - AiService.java → 根据 templateType 查找对应模板的 systemPrompt
 *   - AiController.java → 查询所有模板列表返回给前端
 */
public interface AiTemplateMapper extends BaseMapper<AiTemplate> {
}
