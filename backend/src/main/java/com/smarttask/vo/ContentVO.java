package com.smarttask.vo;

import com.smarttask.entity.Content;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 内容视图对象（VO）
 *
 * 继承自 Content 实体类，在 Content 所有字段的基础上，
 * 额外添加了关联表的字段，供前端展示使用。
 *
 * 为什么用 VO 而不是直接返回 Content 实体？
 *   1. Content 实体类中包含了 deleted 等前端不需要的字段
 *   2. 前端需要展示"分类名称"和"创建者昵称"，这些来自关联表
 *   3. 实体类和 VO 分离，避免数据库结构变动影响前端接口
 *
 * @EqualsAndHashCode(callSuper = true) :
 *   Lombok 注解，生成 equals() 和 hashCode() 时包含父类（Content）的字段。
 *   不加的话只比较子类新增的字段，会导致判断错误。
 *
 * 新增字段说明：
 *   categoryName    - 分类名称，通过 LEFT JOIN ai_content_category 获取
 *                     在 ContentMapper.xml 的 selectContentVOList 中关联
 *   createdByName   - 创建者昵称，通过 LEFT JOIN sys_user 获取
 *                     展示在内容列表中，让用户知道是谁创建的
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ContentVO extends Content {

    private String categoryName;

    private String createdByName;

}
