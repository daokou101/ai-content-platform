package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * 内容版本历史实体类
 *
 * 映射数据库 ai_content_version 表。
 * 每次用户修改并保存内容时，系统自动将当前版本快照存入此表。
 * 支持回滚到任意历史版本。
 *
 * 版本控制机制：
 *   - 首次保存内容 → version = 1
 *   - 每次修改保存 → version 递增 +1
 *   - 回滚操作 → 将选中版本的 title/summary/content 覆盖到当前内容
 *                  同时记录一次新的版本（作为回滚操作本身的一个快照）
 *
 * 使用场景：
 *   - 内容编辑页面 → 右侧"版本历史"面板 → 展示版本列表
 *   - 点击版本 → 预览该版本内容
 *   - 点击"回滚" → 恢复到该版本
 *
 * 属性说明：
 *   id          - 版本记录ID（主键自增）
 *   contentId   - 关联的内容ID，关联 ai_content.id
 *   version     - 版本号，从 1 开始递增，同一 contentId 下唯一
 *   title       - 该版本保存时的标题（快照）
 *   summary     - 该版本保存时的摘要（快照）
 *   content     - 该版本保存时的正文内容（快照）
 *   createdBy   - 创建者用户ID，谁保存的这个版本
 *   createTime  - 版本创建时间
 *
 * @TableName("ai_content_version") : 映射到 ai_content_version 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_content_version")
public class ContentVersion {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long contentId;

    private Integer version;

    private String title;

    private String summary;

    private String content;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
