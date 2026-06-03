package com.smarttask.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

/**
 * AI 生成内容实体类
 *
 * 映射数据库 ai_content 表，存储用户通过 AI 生成的内容。
 * 每次用户在 AI 生成页面点击"保存"后，数据写入此表。
 *
 * 关系说明：
 *   - 一个用户（User）可以拥有多条内容（Content）   → created_by 关联 sys_user.id
 *   - 一条内容（Content）可以属于一个分类（Category）→ category_id 关联 ai_content_category.id
 *   - 一条内容（Content）对应多个版本（ContentVersion）→ 通过 Content.id = ContentVersion.content_id 关联
 *
 * 属性说明：
 *   id             - 内容ID（主键，数据库自增）
 *   title          - 内容标题，用户在保存时自动从关键词中提取第一个词作为标题
 *   summary        - 内容摘要，AI 生成内容的简要概括
 *   content        - 正文内容（LONGTEXT 类型，支持大量文本），AI 生成的完整内容
 *   keywords       - 用户输入的关键词，多个用逗号分隔，例如："Java,Spring Boot,微服务"
 *   templateType   - 使用的模板类型，对应 ai_template.type 字段：
 *                      article   → 文章写作
 *                      marketing → 营销文案
 *                      social    → 社交媒体
 *                      seo       → SEO 优化
 *                      summary   → 内容摘要
 *   categoryId     - 所属分类ID，关联 ai_content_category.id，可为空
 *   status         - 内容状态：draft(草稿) / published(已发布)
 *   favorite       - 收藏标记：0(未收藏) / 1(已收藏)，通过收藏按钮切换
 *   createdBy      - 创建者用户ID，关联 sys_user.id
 *   createTime     - 创建时间，MyBatis-Plus 自动填充（INSERT 时）
 *   updateTime     - 更新时间，MyBatis-Plus 自动填充（INSERT 或 UPDATE 时）
 *   deleted        - 逻辑删除标记：0(未删除) / 1(已删除)
 *                    使用 @TableLogic 注解，MyBatis-Plus 查询时自动拼接 WHERE deleted = 0
 *
 * @TableName("ai_content") : MyBatis-Plus 注解，指定实体类映射的数据库表名
 *   如果类名是 Content，表名是 ai_content，不写注解会默认匹配 content 表
 * @TableId(type = IdType.AUTO) : 主键注解，AUTO 表示使用数据库自增策略
 * @TableLogic : 逻辑删除注解，查询时自动拼接 WHERE deleted = 0
 * @TableField(fill = FieldFill.INSERT) : 自动填充注解，INSERT 时自动设置当前时间
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("ai_content")
public class Content {

    private Long id;
    @TableId(type = IdType.AUTO)
    //id INT AUTO_INCREMENT PRIMARY KEY
    //@TableId = 标记主键
    //IdType.AUTO = 让数据库自动生成自增 ID
    private String title;

    private String summary;

    private String content;

    private String keywords;

    private String templateType;

    private Long categoryId;

    private String status;

    private Integer favorite;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
