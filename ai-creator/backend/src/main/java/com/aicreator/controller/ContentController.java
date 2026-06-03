package com.aicreator.controller;

import com.aicreator.common.api.Result;
import com.aicreator.dto.ContentUpdateDTO;
import com.aicreator.entity.Category;
import com.aicreator.entity.Content;
import com.aicreator.entity.ContentVersion;
import com.aicreator.security.JwtUser;
import com.aicreator.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/contents")
@RequiredArgsConstructor
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public Result<?> list(@RequestParam(defaultValue = "1") int page,
                          @RequestParam(defaultValue = "10") int size,
                          @RequestParam(required = false) String keyword,
                          @RequestParam(required = false) String templateType,
                          @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.getMyContents(user.getUserId(), page, size, keyword, templateType));
    }

    @GetMapping("/{id}")
    public Result<Content> detail(@PathVariable Long id, @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.getById(id, user.getUserId()));
    }

    /**
     * 保存 AI 生成的内容
     */
    @PostMapping
    public Result<Content> save(@RequestBody Map<String, Object> body,
                                @AuthenticationPrincipal JwtUser user) {
        Content c = contentService.saveGenerated(
                user.getUserId(),
                (String) body.get("title"),
                (String) body.get("content"),
                (String) body.get("templateType"),
                (String) body.get("keywords"),
                body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null
        );
        return Result.success(c);
    }

    @PutMapping("/{id}")
    public Result<Content> update(@PathVariable Long id, @RequestBody ContentUpdateDTO dto,
                                  @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.updateContent(id, dto, user.getUserId()));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id, @AuthenticationPrincipal JwtUser user) {
        contentService.deleteContent(id, user.getUserId());
        return Result.success();
    }

    // ===== 版本管理 =====

    @GetMapping("/{id}/versions")
    public Result<List<ContentVersion>> versions(@PathVariable Long id, @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.getVersions(id, user.getUserId()));
    }

    @PostMapping("/{id}/rollback/{version}")
    public Result<Content> rollback(@PathVariable Long id, @PathVariable Integer version,
                                    @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.rollback(id, version, user.getUserId()));
    }

    // ===== 收藏 =====

    @PostMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(@PathVariable Long id, @AuthenticationPrincipal JwtUser user) {
        contentService.toggleFavorite(id, user.getUserId());
        return Result.success();
    }

    // ===== 分类 =====

    @GetMapping("/categories")
    public Result<List<Category>> categories(@AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.getCategories(user.getUserId()));
    }

    @PostMapping("/categories")
    public Result<Category> createCategory(@RequestBody Map<String, String> body,
                                           @AuthenticationPrincipal JwtUser user) {
        return Result.success(contentService.createCategory(user.getUserId(), body.get("name")));
    }

    @DeleteMapping("/categories/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id, @AuthenticationPrincipal JwtUser user) {
        contentService.deleteCategory(id, user.getUserId());
        return Result.success();
    }
}
