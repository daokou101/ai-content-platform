package com.smarttask.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘视图对象
 *
 * 聚合首页仪表盘需要的各种统计数据。
 * 前端 Dashboard.vue 根据这些数据显示欢迎卡片和四项统计。
 *
 * @Builder : Lombok 提供的建造者模式注解，可以链式调用设置属性
 *   DashboardVO.builder().totalContents(10L).todayGenerations(3L).build()
 *
 * 属性说明：
 *   totalContents    - 当前用户的内容总数（不包含已删除的）
 *                      DashboardService → ContentMapper.selectCount
 *   todayGenerations - 今日生成数量（create_time >= 今天零点的记录数）
 *                      DashboardService → ContentMapper.countTodayByUser
 *   templates        - 模板总数（当前固定返回 5，后续可以从 ai_template 表动态查询）
 *                      硬编码为常量，不需要查数据库
 *   favorites        - 收藏内容数（favorite = 1 的记录数）
 *                      DashboardService → ContentMapper.countFavoritesByUser
 *   myPoints         - 当前用户的积分
 *                      DashboardService → sys_user.points 字段
 *   myRank           - 当前用户在积分排行榜中的排名（从 1 开始）
 *                      通过 pointsRanking 列表遍历找到自己的位置
 *   myRole           - 当前用户的角色编码（SUPER_ADMIN / ADMIN / VIP_USER / NORMAL_USER）
 *   pointsRanking    - 积分排行榜前 10 名列表，每个元素包含 id/nickname/avatar/points/role
 *   totalUsers       - 系统总用户数（仅管理员可见）
 *   onlineUsers      - 当前在线用户数（仅管理员可见），基于 Redis 的 Set 统计
 *
 * DashboardController.java 中返回给前端：
 *   GET /api/dashboard → Result.success(dashboardVO)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private Long totalContents;

    private Long todayGenerations;

    private Long templates;

    private Long favorites;

    private Integer myPoints;

    private Integer myRank;

    private String myRole;

    private List<Map<String, Object>> pointsRanking;

    private Long totalUsers;

    private Long onlineUsers;
}
