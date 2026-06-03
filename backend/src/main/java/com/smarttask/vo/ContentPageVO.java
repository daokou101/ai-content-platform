package com.smarttask.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

/**
 * 内容分页视图对象
 *
 * 包装分页查询结果，返回给前端。
 *
 * 前端 ContentList.vue 中期望的格式：
 *   {
 *     "records": [{...}, {...}],  // 当前页的数据列表
 *     "total": 42                 // 总记录数（用于计算总页数）
 *   }
 *
 * 为什么需要这个类？
 *   MyBatis-Plus 的 Page 对象序列化后字段名不匹配前端需求，
 *   通过这个 VO 统一输出格式。
 *
 * @AllArgsConstructor : Lombok 注解，生成全参构造函数，方便 new ContentPageVO(list, total)
 *
 * 属性说明：
 *   records - 当前页的数据列表（List<ContentVO>），前端用 records 字段遍历表格
 *   total   - 总记录数，前端用于计算分页组件总页数
 */
@Data
@AllArgsConstructor
public class ContentPageVO {

    private List<?> records;

    private long total;
}
