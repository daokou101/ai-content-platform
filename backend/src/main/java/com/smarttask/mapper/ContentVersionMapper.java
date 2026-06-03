package com.smarttask.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smarttask.entity.ContentVersion;

/**
 * 内容版本 Mapper 接口
 *
 * 直接使用 BaseMapper 提供的方法。
 * 版本管理相关的复杂逻辑（如获取最新版本号、回滚）在 ContentService 中实现。
 *
 * 提供的基础方法：
 *   - insert(ContentVersion)     → 保存新版本
 *   - selectList(LambdaQueryWrapper) → 按 contentId 查询版本列表（在服务层排序）
 *   - selectOne(LambdaQueryWrapper) → 查询特定版本（用于回滚）
 *
 * 使用到的位置：
 *   - ContentService.java → 通过此 Mapper 操作版本表
 */
public interface ContentVersionMapper extends BaseMapper<ContentVersion> {
}
