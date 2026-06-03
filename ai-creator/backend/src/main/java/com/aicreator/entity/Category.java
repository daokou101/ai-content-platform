package com.aicreator.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 内容分类
 */
@Data
@TableName("ai_category")
public class Category {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String name;           // 分类名称
    private String description;
    private Integer sortOrder;     // 排序
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
