package com.aicreator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 创作内容实体
 *
 * 这是 AI 生成的内容主体，每次生成或手动创建都保存为一条记录
 */
@Data
@TableName("ai_content")
public class Content {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;           // 作者
    private Long categoryId;       // 分类
    private String title;          // 标题
    private String content;        // 内容主体（Markdown 格式）
    private String summary;        // 摘要
    private String templateType;   // 所用模板类型：ARTICLE/SOCIAL_MEDIA/CODE/REPORT/TRANSLATION
    private String keywords;       // 生成时的关键词（逗号分隔）
    private String status;         // 状态：DRAFT(草稿) PUBLISHED(已发布)
    private Integer isFavorite;    // 是否收藏：0否 1是
    private Integer version;       // 当前版本号
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
