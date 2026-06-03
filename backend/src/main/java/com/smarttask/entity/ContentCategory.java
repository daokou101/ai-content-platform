package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 内容分类实体类
 *
 * 映射数据库 ai_content_category 表。
 * 用户创建的自定义分类，用于对 AI 生成的内容进行归类管理。
 * 在内容编辑页面可以使用分类下拉框选择。
 *
 * 使用场景：
 *   - 内容管理页面 → 分类管理弹窗（新增/删除）
 *   - 内容编辑页面 → 选择所属分类
 *
 * 属性说明：
 *   id          - 分类ID（主键自增）
 *   name        - 分类名称，例如："技术文章"、"营销案例"、"个人笔记"
 *   createdBy   - 创建者用户ID，关联 sys_user.id
 *   createTime  - 创建时间
 *   updateTime  - 更新时间
 *   deleted     - 逻辑删除标记：0(未删除) / 1(已删除)
 *
 * @TableName("ai_content_category") : 映射到 ai_content_category 表
 * @Data : Lombok 注解，自动生成 getter/setter/toString/equals/hashCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_content_category")
public class ContentCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    //逻辑删除
    /*
    @TableLogic(
    value = "0",      // 未删除：数据库存 0
    delval = "1"      // 已删除：数据库存 1
)
     */
    private Integer deleted;
}
