package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI 模板实体类
 *
 * 映射数据库 ai_template 表。
 * 定义了 AI 生成内容的"模板类型"，每种模板对应不同的提示词（system_prompt）。
 *
 * 为什么要把模板存数据库而不是硬编码？
 *   1. 方便扩展：新增模板类型只需要 INSERT 一条数据，不需要改代码
 *   2. 动态加载：前端 GET /api/ai/templates 直接从数据库获取模板列表
 *   3. 支持配置：未来可以在管理后台动态编辑模板的提示词
 *
 * 系统内置 5 种模板（在 init.sql 中初始化）：
 *   - article   → 文章写作（system_prompt 要求生成结构完整、800-1500字的文章）
 *   - marketing → 营销文案（要求有说服力、吸引眼球的营销文案）
 *   - social    → 社交媒体（适合微博/小红书的短文案）
 *   - seo       → SEO 优化（合理布局关键词的优化内容）
 *   - summary   → 内容摘要（简洁准确、200字以内的摘要）
 *
 * 使用场景：
 *   - AI 生成页面 → 模板选择（以卡片形式展示）
 *   - 后端 AiService → 根据 templateType 查找对应 system_prompt
 *
 * 属性说明：
 *   id            - 模板ID（主键自增）
 *   type          - 模板类型标识，唯一，如 "article"、"marketing"
 *   name          - 模板显示名称，如 "文章写作"、"营销文案"
 *   description   - 模板描述，展示在前端供用户了解模板用途
 *   systemPrompt  - AI 系统提示词，发送给 DeepSeek 的 system message 内容
 *   sortOrder     - 排序号，数字越小越靠前
 *   createTime    - 创建时间
 *   updateTime    - 更新时间
 *
 * @TableName("ai_template") : 映射到 ai_template 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_template")
public class AiTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String name;

    private String description;

    private String systemPrompt;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
