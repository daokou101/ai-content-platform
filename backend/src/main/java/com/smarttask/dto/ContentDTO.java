package com.smarttask.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内容创建/更新请求 DTO
 *
 * 前端在保存或编辑内容时，将表单数据封装成这个对象发送给后端。
 * 和 Content 实体类的区别：
 *   Content（实体）→ 映射数据库，包含 createdBy、createTime 等后端自动填充的字段
 *   ContentDTO（传输对象）→ 只包含前端可以提交的字段，后端自动生成的字段不在此处
 *
 * DTO 中使用了 Jakarta Validation 注解进行参数校验：
 *   @NotBlank  → 校验字符串不能为 null 且不能是空字符串
 *   @NotNull   → 校验不能为 null
 *   @Size      → 校验字符串长度范围
 * 这些注解在 Controller 中配合 @Valid 或 @Validated 触发校验。
 *
 * 使用到的位置（在即将编写的 ContentController 中）：
 *   - POST   /api/contents            → @Valid @RequestBody ContentDTO 创建
 *   - PUT    /api/contents/{id}        → @Valid @RequestBody ContentDTO 更新
 *
 * 属性说明：
 *   title         - 内容标题，必填（@NotBlank 校验），最大长度在数据库 varchar(255) 限制
 *   summary       - 内容摘要，选填，前端编辑时可手动填写
 *   content       - 正文内容，选填，AI 生成的内容或用户手动编辑的内容
 *   keywords      - 关键词，选填，AI 生成的依据，多个用逗号分隔
 *   templateType  - 模板类型，选填，如 "article"、"marketing"，标识内容是用什么模板生成的
 *   categoryId    - 分类ID，选填，关联 ai_content_category.id
 *   status        - 内容状态，选填，draft(草稿) / published(已发布)
 */
@Data
public class ContentDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String summary;

    private String content;

    private String keywords;

    private String templateType;

    private Long categoryId;

    private String status;
}
