package com.aicreator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 内容版本记录
 *
 * 每次用户编辑内容时自动创建新版本，支持追溯和回滚
 */
@Data
@TableName("ai_content_version")
public class ContentVersion {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;        // 关联内容ID
    private Integer version;       // 版本号（从1递增）
    private String title;          // 该版本标题
    private String content;        // 该版本内容
    private String changeLog;      // 变更说明
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
