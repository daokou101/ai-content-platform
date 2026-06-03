package com.smarttask.controller;

import com.smarttask.common.annotation.Idempotent;
import com.smarttask.common.api.Result;
import com.smarttask.dto.ContentDTO;
import com.smarttask.entity.Content;
import com.smarttask.entity.ContentCategory;
import com.smarttask.entity.ContentVersion;
import com.smarttask.security.CustomUserDetails;
import com.smarttask.service.ContentService;
import com.smarttask.vo.ContentPageVO;
import com.smarttask.vo.ContentVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 内容管理控制器
 *
 * 提供 AI 生成内容的增删改查、版本管理、收藏、分类等接口。
 * 所有接口都需要登录后才能访问（除了白名单中的路径）。
 *
 * @RestController : @Controller + @ResponseBody 的组合注解
 *   所有方法的返回值自动序列化为 JSON 写入 HTTP 响应体
 * @RequestMapping("/api/contents") : 所有接口的路径前缀
 *   - 前端通过 axios 调用时 baseURL=/api，所以完整路径匹配
 *   - 例如：/api/contents?page=1&size=10 对应 getContentList 方法
 * @RequiredArgsConstructor : Lombok 注解，为 final 字段 contentService 生成构造函数注入
 *
 * AuthenticationPrincipal : Spring Security 注解
 *   从 SecurityContextHolder 中获取当前已认证的用户详情
 *   CustomUserDetails 是自定义的 UserDetails 实现类
 *   使用方式：方法参数上加 @AuthenticationPrincipal CustomUserDetails userDetails
 *
 * 使用到的位置：
 *   前端 ContentList.vue / ContentEdit.vue → 通过 API 调用此控制器的接口
 */
@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    // ==================== 内容 CRUD ====================

    /**
     * 创建内容
     *
     * 前端调用时机：
     *   - AI 生成页面 → 点击"保存"按钮 → POST /api/contents
     *   - 内容编辑页面 → 点击"保存"按钮 → POST /api/contents
     *
     * @Idempotent : 自定义幂等性注解（在 common/annotation/Idempotent.java 中定义）
     *   作用：防止用户快速双击保存按钮导致重复提交
     *   原理：前端需要在请求头中传 idempotent-token（可用 UUID 生成）
     *        同一个 token 10 秒内重复请求会被拒绝
     *   使用到的 AOP 类：IdempotentAspect.java（在 aspect/IdempotentAspect.java 中实现）
     *
     * @Valid : Jakarta Validation 注解，触发 ContentDTO 中的 @NotBlank 校验
     *   如果 title 为空，Spring 会自动返回 400 错误，不会进入方法体
     *
     * @RequestBody : 将 HTTP 请求体中的 JSON 反序列化为 ContentDTO
     *   前端发的 JSON：{"title":"...", "summary":"...", "content":"...", ...}
     *
     * @param dto         前端传来的内容数据
     * @param userDetails 当前登录用户详情（由 Spring Security 注入）
     * @return 创建成功的内容实体，包含自增 ID
     */
    @PostMapping
    @Idempotent(message = "内容正在保存中，请勿重复提交")
    public Result<Content> createContent(@Valid @RequestBody ContentDTO dto,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        Content content = contentService.createContent(dto, userDetails.getUserId());
        return Result.success(content);
    }

    /**
     * 分页查询内容列表
     *
     * 前端调用时机：
     *   - 内容管理页面 → 进入页面时 → GET /api/contents?page=1&size=10
     *   - 搜索时 → GET /api/contents?page=1&size=10&keyword=spring
     *   - 筛选时 → GET /api/contents?page=1&size=10&templateType=article
     *
     * @RequestParam : 从 URL 查询参数中取值
     *   defaultValue="1" : 如果前端没传 page 参数，默认为 1
     *   required=false    : 该参数不是必填的
     *
     * 完整的请求示例：
     *   GET /api/contents?page=1&size=10&keyword=Java&templateType=article
     *
     * @param pageNum      页码（从 1 开始）          对应前端分页组件的 current-page
     * @param pageSize     每页条数（默认 10）         对应前端分页组件的 page-size
     * @param keyword      搜索关键词（可为空）        对应前端搜索框的值
     * @param templateType 模板类型筛选（可为空）      对应前端模板类型下拉框的值
     * @param userDetails  当前登录用户详情
     * @return 分页数据（包含 records 列表和 total 总条数）
     */
    @GetMapping
    public Result<ContentPageVO> getContentList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String templateType,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ContentPageVO vo = contentService.getContentPage(page, size, keyword, templateType,
                userDetails.getUserId());
        return Result.success(vo);
    }

    /**
     * 获取内容详情
     *
     * 前端调用时机：
     *   - 点击内容列表中的某条内容 → 进入编辑页面 → GET /api/contents/123
     *   - 内容编辑页面刷新时 → GET /api/contents/123
     *
     * @PathVariable : 从 URL 路径中取值
     *   GET /api/contents/123 → id = 123
     *
     * @param id 内容ID（来自 URL 路径）
     * @return 内容 VO（包含分类名和创建者昵称）
     */
    @GetMapping("/{id}")
    public Result<ContentVO> getContentDetail(@PathVariable Long id) {
        return Result.success(contentService.getContentDetail(id));
    }

    /**
     * 更新内容
     *
     * 前端调用时机：
     *   - 内容编辑页面 → 修改内容后点击"保存" → PUT /api/contents/123
     *
     * @param id          要更新的内容ID（来自 URL 路径）
     * @param dto         新的内容数据
     * @param userDetails 当前登录用户详情
     */
    @PutMapping("/{id}")
    public Result<Void> updateContent(@PathVariable Long id,
                                       @Valid @RequestBody ContentDTO dto,
                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentService.updateContent(id, dto, userDetails.getUserId());
        return Result.success();
    }

    /**
     * 删除内容（逻辑删除）
     *
     * 前端调用时机：
     *   - 内容管理列表 → 点击某条内容的"删除"按钮 → DELETE /api/contents/123
     *
     * @param id          要删除的内容ID
     * @param userDetails 当前登录用户详情
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteContent(@PathVariable Long id,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentService.deleteContent(id, userDetails.getUserId());
        return Result.success();
    }

    // ==================== 收藏 ====================

    /**
     * 切换收藏状态
     *
     * 前端调用时机：
     *   - 内容管理列表 → 点击某条内容的"收藏"图标 → POST /api/contents/123/favorite
     *
     * 这是一个"切换"操作，每次调用都会翻转收藏状态：
     *   未收藏 → 收藏
     *   已收藏 → 取消收藏
     *
     * 为什么不用 PUT 或 PATCH？
     *   因为这是"切换"语义，不是"设置"语义。POST 最适合表示"执行一个动作"。
     *
     * @param id          内容ID
     * @param userDetails 当前登录用户详情
     */
    @PostMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentService.toggleFavorite(id, userDetails.getUserId());
        return Result.success();
    }

    // ==================== 分类管理 ====================

    /**
     * 获取分类列表
     *
     * 前端调用时机：
     *   - 内容编辑页面 → 加载分类下拉框 → GET /api/contents/categories
     *   - 内容管理页面 → 打开"分类管理"弹窗 → GET /api/contents/categories
     *
     * 注意：这个接口的路径 GET /api/contents/categories 中 categories 不是动态参数，
     * 所以必须放在 GET /api/contents/{id} 之前定义（Spring 匹配时优先精确匹配），
     * 否则 Spring 会把 "categories" 当成 {id}。
     * 但这里顺序是对的，因为 /{id} 在 /categories 下面定义。
     * 实际上 Spring 5+ 已经处理了这个问题，不用太担心顺序。
     *
     * @return 分类列表
     */
    @GetMapping("/categories")
    public Result<List<ContentCategory>> getCategories() {
        return Result.success(contentService.listCategories());
    }

    /**
     * 创建分类
     *
     * 前端调用时机：
     *   - 内容管理页面 → "分类管理"弹窗 → 输入分类名 → 点击"新增"
     *
     * @RequestBody Map<String, String> body : 前端传的 JSON 对象，这里只取 name 字段
     *   前端发的内容：{"name": "技术文章"}
     *   后端用 body.get("name") 取出
     *
     * @param body        包含 name 字段的 Map
     * @param userDetails 当前登录用户详情
     * @return 创建成功的分类
     */
    @PostMapping("/categories")
    public Result<ContentCategory> createCategory(@RequestBody Map<String, String> body,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        return Result.success(contentService.createCategory(body.get("name"), userDetails.getUserId()));
    }

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        contentService.deleteCategory(id);
        return Result.success();
    }

    // ==================== 版本管理 ====================

    /**
     * 获取内容的版本历史
     *
     * 前端调用时机：
     *   - 内容编辑页面 → 右侧"版本历史"面板 → GET /api/contents/123/versions
     *
     * 返回的版本列表按版本号倒序排列（最新的在最上面）。
     * 每条版本记录包含：版本号、标题、摘要、创建时间、创建者。
     *
     * @param id 内容ID
     * @return 版本历史列表
     */
    @GetMapping("/{id}/versions")
    public Result<List<ContentVersion>> getVersions(@PathVariable Long id) {
        return Result.success(contentService.getVersions(id));
    }

    /**
     * 回滚到指定版本
     *
     * 前端调用时机：
     *   - 内容编辑页面 → "版本历史"面板 → 点击某版本的"回滚"按钮
     *     → POST /api/contents/123/rollback/2
     *
     * @param id            内容ID
     * @param versionNumber 要回滚到的版本号（来自 URL 路径）
     * @param userDetails   当前登录用户详情
     */
    @PostMapping("/{id}/rollback/{versionNumber}")
    public Result<Void> rollback(@PathVariable Long id,
                                  @PathVariable Integer versionNumber,
                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        contentService.rollback(id, versionNumber, userDetails.getUserId());
        return Result.success();
    }
}
