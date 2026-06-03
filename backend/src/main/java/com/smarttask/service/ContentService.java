package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smarttask.common.api.ResultCode;
import com.smarttask.common.constant.CommonConstants;
import com.smarttask.common.exception.BusinessException;
import com.smarttask.dto.ContentDTO;
import com.smarttask.entity.Content;
import com.smarttask.entity.ContentCategory;
import com.smarttask.entity.ContentVersion;
import com.smarttask.mapper.ContentCategoryMapper;
import com.smarttask.mapper.ContentMapper;
import com.smarttask.mapper.ContentVersionMapper;
import com.smarttask.vo.ContentPageVO;
import com.smarttask.vo.ContentVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 内容管理服务
 *
 * 负责 AI 生成内容的 CRUD、版本控制、收藏、分类等功能。
 * 是系统的核心业务服务，和 ContentController 一一对应。
 *
 * @Transactional : Spring 声明式事务注解
 *   rollbackFor = Exception.class : 任何异常都回滚（默认只回滚 RuntimeException）
 *   用在需要保证数据一致性的方法上（创建、更新、删除）
 *
 * @RequiredArgsConstructor : Lombok 注解，为所有 final 字段生成构造函数
 *   Spring 自动注入 ContentMapper、ContentVersionMapper、ContentCategoryMapper
 *
 * 使用到的外部类：
 *   ContentMapper          → 操作 ai_content 表（自定义 SQL 在 ContentMapper.xml）
 *   ContentVersionMapper   → 操作 ai_content_version 表（BaseMapper 方法足够）
 *   ContentCategoryMapper  → 操作 ai_content_category 表（BaseMapper 方法足够）
 */
@Service
@RequiredArgsConstructor
public class ContentService {

    /** 内容数据访问对象，操作 ai_content 表 */
    private final ContentMapper contentMapper;

    /** 版本历史数据访问对象，操作 ai_content_version 表 */
    private final ContentVersionMapper contentVersionMapper;

    /** 分类数据访问对象，操作 ai_content_category 表 */
    private final ContentCategoryMapper contentCategoryMapper;

    // ==================== 内容 CRUD ====================

    /**
     * 创建内容
     *
     * 用户点击"保存"时调用。
     * 流程：
     *   1. 将 DTO 转为 Content 实体
     *   2. 设置初始值（createdBy、status=draft、favorite=0）
     *   3. INSERT 到 ai_content 表
     *   4. 创建版本 1 的快照存入 ai_content_version 表
     *
     * @Transactional : 两个 INSERT 在一个事务中，任何一个失败都回滚
     *
     * @param dto    前端传来的内容数据（标题、摘要、正文、关键词、模板类型、分类ID、状态）
     * @param userId 当前登录用户ID（从 JWT 中解析）
     * @return 创建成功后的 Content 实体（包含自增的 id）
     */
    @Transactional(rollbackFor = Exception.class)
    public Content createContent(ContentDTO dto, Long userId) {
        // 构建 Content 实体
        // 这里直接手动 set，不用的字段保持 null（数据库默认值或可为空）
        Content content = new Content();
        content.setTitle(dto.getTitle());
        content.setSummary(dto.getSummary());
        content.setContent(dto.getContent());
        content.setKeywords(dto.getKeywords());
        content.setTemplateType(dto.getTemplateType());
        content.setCategoryId(dto.getCategoryId());
        // 如果前端没传 status，默认草稿
        content.setStatus(dto.getStatus() != null ? dto.getStatus() : CommonConstants.CONTENT_STATUS_DRAFT);
        content.setFavorite(0);   // 新创建的内容默认未收藏
        content.setCreatedBy(userId);

        // INSERT 到数据库，执行后 content.getId() 会返回自增的 ID
        contentMapper.insert(content);

        // 创建版本 1 的快照
        saveVersionSnapshot(content, 1);

        return content;
    }

    /**
     * 更新内容
     *
     * 用户在编辑页面修改内容后保存时调用。
     * 流程：
     *   1. 检查内容是否存在
     *   2. 检查操作者是否为内容的创建者
     *   3. 将当前内容保存为新版本（快照）
     *   4. 更新内容字段
     *
     * @param id     要更新的内容ID
     * @param dto    新的内容数据
     * @param userId 当前登录用户ID（用于权限校验）
     * @throws BusinessException 内容不存在或无权操作时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateContent(Long id, ContentDTO dto, Long userId) {
        Content content = findContentById(id);

        // 权限校验：只能编辑自己的内容
        if (!content.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权编辑此内容");
        }

        // 保存当前版本快照（版本号 = 当前最大版本号 + 1）
        Integer currentMaxVersion = getMaxVersion(id);
        saveVersionSnapshot(content, currentMaxVersion + 1);

        // 更新字段
        if (dto.getTitle() != null) content.setTitle(dto.getTitle());
        if (dto.getSummary() != null) content.setSummary(dto.getSummary());
        if (dto.getContent() != null) content.setContent(dto.getContent());
        if (dto.getKeywords() != null) content.setKeywords(dto.getKeywords());
        if (dto.getTemplateType() != null) content.setTemplateType(dto.getTemplateType());
        if (dto.getCategoryId() != null) content.setCategoryId(dto.getCategoryId());
        if (dto.getStatus() != null) content.setStatus(dto.getStatus());

        contentMapper.updateById(content);
    }

    /**
     * 删除内容（逻辑删除）
     *
     * @Transactional 保证以下操作原子性：
     *   虽然这里只调用了 deleteById，但将来可能扩展为同时删除版本记录
     *
     * @param id     要删除的内容ID
     * @param userId 当前登录用户ID（用于权限校验）
     * @throws BusinessException 内容不存在或无权操作时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteContent(Long id, Long userId) {
        Content content = findContentById(id);

        // 权限校验：只能删除自己的内容
        if (!content.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权删除此内容");
        }

        // @TableLogic 注解会自动将 deleted 字段更新为 1，而不是真的 DELETE
        contentMapper.deleteById(id);
    }

    /**
     * 分页查询内容列表
     *
     * MyBatis-Plus 分页原理：
     *   Page 对象传到 Mapper 后，PaginationInterceptor 自动做两件事：
     *     1. 执行 COUNT 查询获取总条数（填充 page.getTotal()）
     *     2. 在原 SQL 追加 LIMIT 语句（填充 page.getRecords()）
     *
     * @param pageNum      页码（从 1 开始）
     * @param pageSize     每页条数
     * @param keyword      搜索关键词（模糊匹配标题和关键词），可为 null
     * @param templateType 模板类型筛选，可为 null
     * @param userId       当前用户ID（数据隔离）
     * @return ContentPageVO 包含 records（当前页数据）和 total（总条数）
     */
    public ContentPageVO getContentPage(int pageNum, int pageSize, String keyword, String templateType, Long userId) {
        // 创建分页对象，Page<ContentVO> 的泛型要和返回的列表类型一致
        Page<ContentVO> page = new Page<>(pageNum, pageSize);

        // 调用自定义分页查询
        // Page 参数放在第一位，MyBatis-Plus 自动识别并处理分页
        List<ContentVO> records = contentMapper.selectContentVOList(
                page, keyword, templateType, userId
        );

        // page.getTotal() 在 selectContentVOList 执行后被 MyBatis-Plus 自动填充
        return new ContentPageVO(records, page.getTotal());
    }

    /**
     * 获取内容详情
     *
     * @param id 内容ID
     * @return 内容 VO（包含分类名和创建者昵称）
     * @throws BusinessException 内容不存在时抛出
     */
    public ContentVO getContentDetail(Long id) {
        ContentVO vo = contentMapper.selectContentVODetail(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND);
        }
        return vo;
    }

    // ==================== 收藏 ====================

    /**
     * 切换内容收藏状态
     *
     * 如果当前是收藏状态（favorite=1）则取消收藏（favorite=0）；
     * 如果当前是未收藏状态（favorite=0）则收藏（favorite=1）。
     *
     * @param id     内容ID
     * @param userId 当前用户ID（用于权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleFavorite(Long id, Long userId) {
        Content content = findContentById(id);

        // 权限校验：只能操作自己的内容
        if (!content.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此内容");
        }

        // 翻转 favorite 值：0→1 或 1→0
        // 这里用 XOR 位运算：favorite ^ 1 实现翻转
        content.setFavorite(content.getFavorite() ^ 1);
        contentMapper.updateById(content);
    }

    // ==================== 分类管理 ====================

    /**
     * 获取分类列表
     *
     * 目前返回所有分类（不区分用户）。
     * 如果将来想实现用户隔离，可以加 WHERE created_by = userId。
     *
     * @return 分类列表
     */
    public List<ContentCategory> listCategories() {
        return contentCategoryMapper.selectList(null);
    }

    /**
     * 创建分类
     *
     * @param name   分类名称，不能为空
     * @param userId 创建者用户ID
     * @return 创建成功的分类实体（包含自增的 id）
     */
    @Transactional(rollbackFor = Exception.class)
    public ContentCategory createCategory(String name, Long userId) {
        ContentCategory category = new ContentCategory();
        category.setName(name);
        category.setCreatedBy(userId);
        contentCategoryMapper.insert(category);
        return category;
    }

    /**
     * 删除分类（逻辑删除）
     *
     * 删除分类不会删除该分类下的内容，只是内容不再显示分类名。
     *
     * @param id 分类ID
     * @throws BusinessException 分类不存在时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 先查一下是否存在，不存在就抛异常
        ContentCategory category = contentCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        contentCategoryMapper.deleteById(id);
    }

    // ==================== 版本管理 ====================

    /**
     * 获取内容的版本历史列表
     *
     * 按版本号倒序排列，最新的版本在最上面。
     *
     * @param contentId 内容ID
     * @return 版本历史列表
     */
    public List<ContentVersion> getVersions(Long contentId) {
        // LambdaQueryWrapper 构造排序条件
        // eq(ContentVersion::getContentId, contentId) → WHERE content_id = contentId
        // orderByDesc(ContentVersion::getVersion)     → ORDER BY version DESC
        LambdaQueryWrapper<ContentVersion> wrapper = new LambdaQueryWrapper<ContentVersion>()
                .eq(ContentVersion::getContentId, contentId)
                .orderByDesc(ContentVersion::getVersion);

        return contentVersionMapper.selectList(wrapper);
    }

    /**
     * 回滚到指定版本
     *
     * 将内容的 title、summary、content 恢复到指定版本时的状态。
     * 同时保存一个"回滚快照"作为新版本，保证可追溯。
     *
     * 流程：
     *   1. 查当前内容（验证所有权）
     *   2. 查目标版本记录
     *   3. 将当前状态保存为新版本（防止回滚后想找回当前内容）
     *   4. 用目标版本的数据覆盖当前内容
     *   5. UPDATE 数据库
     *   6. 再将回滚后的状态保存为一个新版本
     *
     * @param contentId     内容ID
     * @param versionNumber 要回滚到的目标版本号
     * @param userId        当前用户ID（权限校验）
     * @throws BusinessException 内容/版本不存在或无权操作时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void rollback(Long contentId, Integer versionNumber, Long userId) {
        Content content = findContentById(contentId);

        // 权限校验：只能回滚自己的内容
        if (!content.getCreatedBy().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无权操作此内容");
        }

        // 查找目标版本
        LambdaQueryWrapper<ContentVersion> versionQuery = new LambdaQueryWrapper<ContentVersion>()
                .eq(ContentVersion::getContentId, contentId)
                .eq(ContentVersion::getVersion, versionNumber);
        ContentVersion targetVersion = contentVersionMapper.selectOne(versionQuery);

        if (targetVersion == null) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND, "版本 " + versionNumber + " 不存在");
        }

        // 先把当前状态保存为新版本（快照），防止误回滚后丢失当前内容
        Integer currentMaxVersion = getMaxVersion(contentId);
        saveVersionSnapshot(content, currentMaxVersion + 1);

        // 用目标版本的数据覆盖当前内容
        content.setTitle(targetVersion.getTitle());
        content.setSummary(targetVersion.getSummary());
        content.setContent(targetVersion.getContent());
        contentMapper.updateById(content);

        // 再将回滚后的状态保存为新版本（回滚快照）
        Integer afterRollbackVersion = getMaxVersion(contentId) + 1;
        saveVersionSnapshot(content, afterRollbackVersion);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 根据 ID 查找内容
     *
     * @param id 内容ID
     * @return Content 实体
     * @throws BusinessException 如果内容不存在或已删除
     */
    private Content findContentById(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ResultCode.CONTENT_NOT_FOUND);
        }
        return content;
    }

    /**
     * 获取某个内容的当前最大版本号
     *
     * 如果没有任何版本记录，返回 0（这样 0+1 = 1 就是第一个版本）。
     *
     * @param contentId 内容ID
     * @return 当前最大版本号（没有版本时返回 0）
     */
    private Integer getMaxVersion(Long contentId) {
        LambdaQueryWrapper<ContentVersion> wrapper = new LambdaQueryWrapper<ContentVersion>()
                .eq(ContentVersion::getContentId, contentId)
                .orderByDesc(ContentVersion::getVersion)
                .last("LIMIT 1");

        ContentVersion latest = contentVersionMapper.selectOne(wrapper);
        return latest != null ? latest.getVersion() : 0;
    }

    /**
     * 保存内容快照到版本表
     *
     * 每次内容发生变化时调用，记录当前状态作为历史快照。
     *
     * @param content 当前内容实体（从中提取 title、summary、content）
     * @param version 版本号（从 1 开始递增）
     */
    private void saveVersionSnapshot(Content content, Integer version) {
        ContentVersion versionRecord = new ContentVersion();
        versionRecord.setContentId(content.getId());
        versionRecord.setVersion(version);
        versionRecord.setTitle(content.getTitle());
        versionRecord.setSummary(content.getSummary());
        versionRecord.setContent(content.getContent());
        versionRecord.setCreatedBy(content.getCreatedBy());
        // createTime 由 MyMetaObjectHandler 自动填充
        contentVersionMapper.insert(versionRecord);
    }
}
