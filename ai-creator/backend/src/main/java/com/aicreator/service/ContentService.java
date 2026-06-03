package com.aicreator.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.aicreator.common.api.ResultCode;
import com.aicreator.common.exception.BusinessException;
import com.aicreator.dto.ContentUpdateDTO;
import com.aicreator.entity.Category;
import com.aicreator.entity.Content;
import com.aicreator.entity.ContentVersion;
import com.aicreator.mapper.CategoryMapper;
import com.aicreator.mapper.ContentMapper;
import com.aicreator.mapper.ContentVersionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ContentService {
    private final ContentMapper contentMapper;
    private final ContentVersionMapper versionMapper;
    private final CategoryMapper categoryMapper;

    // ===== 内容管理 =====

    public IPage<Content> getMyContents(Long userId, int pageNum, int pageSize, String keyword, String templateType) {
        Page<Content> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Content> w = new LambdaQueryWrapper<Content>().eq(Content::getUserId, userId);
        if (keyword != null && !keyword.isEmpty()) w.like(Content::getTitle, keyword).or().like(Content::getContent, keyword);
        if (templateType != null && !templateType.isEmpty()) w.eq(Content::getTemplateType, templateType);
        w.orderByDesc(Content::getUpdateTime);
        return contentMapper.selectPage(page, w);
    }

    public Content getById(Long id, Long userId) {
        Content c = contentMapper.selectById(id);
        if (c == null || !c.getUserId().equals(userId)) throw new BusinessException(ResultCode.NOT_FOUND, "内容不存在");
        return c;
    }

    /**
     * 保存 AI 生成的内容
     */
    @Transactional
    public Content saveGenerated(Long userId, String title, String content, String templateType, String keywords, Long categoryId) {
        Content c = new Content();
        c.setUserId(userId);
        c.setTitle(title);
        c.setContent(content);
        c.setTemplateType(templateType);
        c.setKeywords(keywords);
        c.setCategoryId(categoryId);
        c.setStatus("DRAFT");
        c.setVersion(1);
        contentMapper.insert(c);
        // 自动创建初始版本
        saveVersion(c.getId(), 1, title, content, "AI 生成");
        return c;
    }

    /**
     * 编辑内容（自动创建新版本）
     */
    @Transactional
    public Content updateContent(Long id, ContentUpdateDTO dto, Long userId) {
        Content c = getById(id, userId);
        if (dto.getTitle() != null) c.setTitle(dto.getTitle());
        if (dto.getContent() != null) c.setContent(dto.getContent());
        if (dto.getSummary() != null) c.setSummary(dto.getSummary());
        if (dto.getCategoryId() != null) c.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null) c.setStatus(dto.getStatus());
        c.setVersion(c.getVersion() + 1);
        contentMapper.updateById(c);
        // 保存新版本
        saveVersion(c.getId(), c.getVersion(), c.getTitle(), c.getContent(),
                dto.getChangeLog() != null ? dto.getChangeLog() : "编辑更新");
        return c;
    }

    // ===== 版本管理 =====

    public List<ContentVersion> getVersions(Long contentId, Long userId) {
        getById(contentId, userId); // 验证权限
        return versionMapper.selectList(
                new LambdaQueryWrapper<ContentVersion>().eq(ContentVersion::getContentId, contentId)
                        .orderByDesc(ContentVersion::getVersion));
    }

    /**
     * 回滚到指定版本
     */
    @Transactional
    public Content rollback(Long contentId, Integer version, Long userId) {
        Content c = getById(contentId, userId);
        ContentVersion v = versionMapper.selectOne(
                new LambdaQueryWrapper<ContentVersion>()
                        .eq(ContentVersion::getContentId, contentId)
                        .eq(ContentVersion::getVersion, version));
        if (v == null) throw new BusinessException("版本不存在");
        c.setTitle(v.getTitle());
        c.setContent(v.getContent());
        c.setVersion(c.getVersion() + 1);
        contentMapper.updateById(c);
        saveVersion(c.getId(), c.getVersion(), c.getTitle(), c.getContent(), "回滚到版本 " + version);
        return c;
    }

    private void saveVersion(Long contentId, Integer version, String title, String content, String changeLog) {
        ContentVersion v = new ContentVersion();
        v.setContentId(contentId);
        v.setVersion(version);
        v.setTitle(title);
        v.setContent(content);
        v.setChangeLog(changeLog);
        versionMapper.insert(v);
    }

    // ===== 收藏 =====

    public void toggleFavorite(Long id, Long userId) {
        Content c = getById(id, userId);
        c.setIsFavorite(c.getIsFavorite() != null && c.getIsFavorite() == 1 ? 0 : 1);
        contentMapper.updateById(c);
    }

    // ===== 分类 =====

    public List<Category> getCategories(Long userId) {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getUserId, userId).orderByAsc(Category::getSortOrder));
    }

    public Category createCategory(Long userId, String name) {
        Category c = new Category();
        c.setUserId(userId); c.setName(name);
        categoryMapper.insert(c);
        return c;
    }

    public void deleteCategory(Long id, Long userId) {
        categoryMapper.delete(
                new LambdaQueryWrapper<Category>().eq(Category::getId, id).eq(Category::getUserId, userId));
    }

    public void deleteContent(Long id, Long userId) {
        Content c = getById(id, userId);
        contentMapper.deleteById(id);
    }
}
