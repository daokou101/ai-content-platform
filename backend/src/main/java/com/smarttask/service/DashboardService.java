package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.entity.Content;
import com.smarttask.entity.User;
import com.smarttask.mapper.ContentMapper;
import com.smarttask.mapper.UserMapper;
import com.smarttask.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表盘服务
 *
 * 负责聚合首页仪表盘需要的各种统计数据。
 * 原先是统计任务数据（Task），现在改为统计内容数据（Content）。
 *
 * @RequiredArgsConstructor :
 *   Lombok 注解，为所有 final 字段生成构造函数。
 *   Spring 会自动通过此构造器注入 ContentMapper、UserMapper、StringRedisTemplate。
 *
 * 数据来源汇总：
 *   - 内容统计 → ContentMapper（数据库查询）
 *   - 积分排名 → UserMapper（数据库查询，按积分倒序）
 *   - 在线用户 → Redis（StringRedisTemplate 操作 Set）
 *
 * 使用到的位置：
 *   - DashboardController.getDashboard() → GET /api/dashboard
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /**
     * final 字段说明（由 @RequiredArgsConstructor 注入）：
     *   contentMapper     → 操作 ai_content 表，查询内容统计数据
     *   userMapper        → 操作 sys_user 表，查询用户积分和排名
     *   redisTemplate     → 操作 Redis，查询在线用户数
     */
    private final ContentMapper contentMapper;
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;

    /**
     * 构建仪表盘数据
     *
     * 根据当前登录用户的 ID 和角色，收集并返回仪表盘所需的所有统计数据。
     * 管理员能看到更多信息（总用户数、在线用户数）。
     *
     * @param userId 当前登录用户的 ID（从 JWT 中解析，由 @AuthenticationPrincipal 传入）
     * @param role   当前用户的角色编码（SUPER_ADMIN / ADMIN / NORMAL_USER 等）
     * @return DashboardVO 包含所有统计数据
     *
     * 调用链路：
     *   HTTP GET /api/dashboard
     *     → DashboardController.getDashboard()
     *       → DashboardService.getDashboard(userId, role)
     *         → ContentMapper.selectCount(...)     → 内容总数
     *         → ContentMapper.countTodayByUser()   → 今日生成
     *         → ContentMapper.countFavoritesByUser() → 收藏数
     *         → UserMapper.selectById()            → 用户积分
     *         → this.getPointsRanking()            → 积分排行榜
     *         → StringRedisTemplate.opsForSet().size() → 在线用户（管理员）
     */
    public DashboardVO getDashboard(Long userId, String role) {
        // isAdmin：判断当前用户是否为管理员（超级管理员或普通管理员）
        // 后续用来决定是否展示 totalUsers 和 onlineUsers
        boolean isAdmin = CommonConstants.ROLE_SUPER_ADMIN.equals(role)
                || CommonConstants.ROLE_ADMIN.equals(role);

        // ========== 内容统计 ==========
        // LambdaQueryWrapper：MyBatis-Plus 的条件构造器
        // 这里 eq(Content::getCreatedBy, userId) → WHERE created_by = userId
        // selectCount 会自动拼接逻辑删除条件（WHERE deleted = 0）
        Long totalContents = contentMapper.selectCount(
                new LambdaQueryWrapper<Content>()
                        .eq(Content::getCreatedBy, userId)
        );

        // 今日生成数 → 调用自定义 Mapper 方法
        Long todayGenerations = contentMapper.countTodayByUser(userId);

        // 模板数 → 固定返回 5（article/marketing/social/seo/summary）
        // 性能优化：这不需要查数据库，因为模板数量基本不变
        Long templates = 5L;

        // 收藏数 → 调用自定义 Mapper 方法
        Long favorites = contentMapper.countFavoritesByUser(userId);

        // ========== 个人数据 ==========
        User currentUser = userMapper.selectById(userId);

        // ========== 积分排行榜 ==========
        List<Map<String, Object>> ranking = getPointsRanking();

        // 计算当前用户在排行榜中的排名
        // 遍历 ranking 列表，找到 id 等于 userId 的那一项
        int myRank = 0;
        for (int i = 0; i < ranking.size(); i++) {
            if (Objects.equals(ranking.get(i).get("id"), userId)) {
                myRank = i + 1; // 排名从 1 开始
                break;
            }
        }

        // ========== 使用 Builder 构建 DashboardVO ==========
        // 链式调用：每个 builder.xxx() 返回 builder 自身
        DashboardVO.DashboardVOBuilder builder = DashboardVO.builder()
                .totalContents(totalContents)
                .todayGenerations(todayGenerations)
                .templates(templates)
                .favorites(favorites)
                .myPoints(currentUser != null ? currentUser.getPoints() : 0)
                .myRank(myRank)
                .myRole(role)
                .pointsRanking(ranking);

        // ========== 管理员专属数据 ==========
        if (isAdmin) {
            // 总用户数：select count(*) from sys_user
            Long totalUsers = userMapper.selectCount(new LambdaQueryWrapper<>());

            // 在线用户数：从 Redis 的 Set 结构中获取大小
            // REDIS_ONLINE_USERS = "online:users" （在 CommonConstants 中定义）
            // 用户在登录时会被加入此 Set，登出时移除
            Long onlineCount = Optional.ofNullable(
                    redisTemplate.opsForSet().size(CommonConstants.REDIS_ONLINE_USERS)
            ).orElse(0L);

            builder.totalUsers(totalUsers).onlineUsers(onlineCount);
        }

        // build() 方法由 @Builder 注解自动生成
        return builder.build();
    }

    /**
     * 获取积分排行榜（前 10 名）
     *
     * 按积分从高到低排序，取前 10 条用户记录。
     * 返回一个 List<Map>，每个 Map 包含 id/nickname/avatar/points/role。
     *
     * 使用 LinkedHashMap 保持插入顺序，确保前端渲染顺序一致。
     *
     * @return 排行榜列表，按 points 倒序
     */
    private List<Map<String, Object>> getPointsRanking() {
        // LambdaQueryWrapper<User> 是 MyBatis-Plus 的泛型条件构造器
        // .orderByDesc(User::getPoints)  → ORDER BY points DESC
        // .last("LIMIT 10")              → 追加 LIMIT 10（MyBatis-Plus 方言限制）
        List<User> topUsers = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .orderByDesc(User::getPoints)
                        .last("LIMIT 10")
        );

        // 将 User 实体列表转换为前端需要的 Map 格式
        // 只选择需要返回的字段，避免暴露 password 等敏感信息
        return topUsers.stream().map(u -> {
            // LinkedHashMap：保持放入顺序（id → nickname → avatar → points → role）
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", u.getId());
            map.put("nickname", u.getNickname() != null ? u.getNickname() : u.getUsername());
            map.put("avatar", u.getAvatar());
            map.put("points", u.getPoints());
            map.put("role", u.getRole());
            return map;
        }).collect(Collectors.toList());
    }
}
