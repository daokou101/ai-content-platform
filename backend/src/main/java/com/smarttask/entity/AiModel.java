package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI 模型实体类
 *
 * 映射数据库 ai_model 表。
 * 维护可用的 AI 大模型列表，支持在前端下拉框中切换不同模型。
 *
 * 当前支持的模型（通过 init.sql 初始化）：
 *   - deepseek-chat    → DeepSeek 最新对话模型（默认选中）
 *   - deepseek-reasoner → DeepSeek 推理模型
 *
 * 扩展方式：后续如果想接入通义千问、智谱等国内模型，
 * 只需在此表插入新记录，并实现对应的 API 客户端即可。
 *
 * 使用场景：
 *   - AI 生成页面 → "选择模型"下拉框 → 用户可以选择使用哪个模型生成内容
 *   - 后端 DeepSeekClient → 根据 model 字段值拼接 API 请求参数
 *
 * 属性说明：
 *   id          - 模型ID（主键自增）
 *   name        - 模型显示名称，前端下拉框看到的，如 "DeepSeek Chat"
 *   model       - 模型标识，传给 API 的参数值，如 "deepseek-chat"
 *   provider    - 模型提供商，如 "deepseek"、"aliyun"、"zhipu"
 *   description - 模型描述，便于用户了解模型特点
 *   isDefault   - 是否默认选中：0(否) / 1(是)，前端加载时默认选中的模型
 *   sortOrder   - 排序号，数字越小越靠前
 *   createTime  - 创建时间
 *
 * @TableName("ai_model") : 映射到 ai_model 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_model")
public class AiModel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String model;

    private String provider;

    private String description;

    private Integer isDefault;

    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    //插入时自动填充：创建时间
    private LocalDateTime createTime;
}
