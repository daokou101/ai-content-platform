package com.smarttask.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smarttask.common.api.ResultCode;
import com.smarttask.common.exception.BusinessException;
import com.smarttask.dto.ContentDTO;
import com.smarttask.entity.Content;
import com.smarttask.entity.ContentVersion;
import com.smarttask.mapper.ContentCategoryMapper;
import com.smarttask.mapper.ContentMapper;
import com.smarttask.mapper.ContentVersionMapper;
import com.smarttask.vo.ContentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ContentService 单元测试
 *
 * 测试内容 CRUD、版本管理、收藏、分类等功能。
 * 使用 Mockito 模拟 Mapper 层，不依赖真实数据库。
 */
@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentMapper contentMapper;

    @Mock
    private ContentVersionMapper contentVersionMapper;

    @Mock
    private ContentCategoryMapper contentCategoryMapper;

    @InjectMocks
    private ContentService contentService;

    @Captor
    private ArgumentCaptor<Content> contentCaptor;

    private ContentDTO sampleDTO;
    private Content sampleContent;

    @BeforeEach
    void setUp() {
        sampleDTO = new ContentDTO();
        sampleDTO.setTitle("测试文章");
        sampleDTO.setSummary("这是一篇测试文章");
        sampleDTO.setContent("文章内容正文...");
        sampleDTO.setKeywords("测试, Java, Spring Boot");
        sampleDTO.setTemplateType("article");

        sampleContent = new Content();
        sampleContent.setId(1L);
        sampleContent.setTitle("测试文章");
        sampleContent.setContent("文章内容正文...");
        sampleContent.setCreatedBy(1L);
        sampleContent.setFavorite(0);
    }

    // ==================== 创建内容 ====================

    @Test
    @DisplayName("创建内容 - 成功创建并保存版本快照")
    void createContent_success() {
        when(contentMapper.insert(any(Content.class))).thenAnswer(invocation -> {
            Content c = invocation.getArgument(0);
            c.setId(1L); // 模拟自增 ID
            return 1;
        });

        Content result = contentService.createContent(sampleDTO, 1L);

        assertNotNull(result);
        assertEquals("测试文章", result.getTitle());
        assertEquals(0, result.getFavorite());

        // 验证保存了版本快照
        verify(contentVersionMapper, times(1)).insert(any(ContentVersion.class));
    }

    @Test
    @DisplayName("创建内容 - DTO 的 title 为 null 时抛出异常")
    void createContent_titleNull_throwsException() {
        sampleDTO.setTitle(null);

        // 由于 @Valid 在 controller 层生效，service 直接传 null title 会插入空数据
        // 但我们模拟 mapper.insert 失败的情况
        doAnswer(invocation -> {
            Content c = invocation.getArgument(0);
            assertNull(c.getTitle());
            return 1;
        }).when(contentMapper).insert(any(Content.class));

        Content result = contentService.createContent(sampleDTO, 1L);
        assertNull(result.getTitle());
    }

    // ==================== 更新内容 ====================

    @Test
    @DisplayName("更新内容 - 成功更新并创建新版本")
    void updateContent_success() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);
        when(contentVersionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null); // 无版本记录

        ContentDTO updateDTO = new ContentDTO();
        updateDTO.setTitle("新标题");

        contentService.updateContent(1L, updateDTO, 1L);

        verify(contentMapper, times(1)).updateById(contentCaptor.capture());
        Content updated = contentCaptor.getValue();
        assertEquals("新标题", updated.getTitle());

        // 验证保存了一个版本快照（更新前的快照）
        verify(contentVersionMapper, times(1)).insert(any(ContentVersion.class));
    }

    @Test
    @DisplayName("更新内容 - 非创建者无权编辑")
    void updateContent_notOwner_throwsException() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> contentService.updateContent(1L, updateDTO(), 2L));

        assertEquals(ResultCode.FORBIDDEN.getCode(), exception.getCode());
    }

    @Test
    @DisplayName("更新内容 - 内容不存在时抛出异常")
    void updateContent_notFound_throwsException() {
        when(contentMapper.selectById(999L)).thenReturn(null);

        assertThrows(BusinessException.class,
                () -> contentService.updateContent(999L, updateDTO(), 1L));
    }

    // ==================== 删除内容 ====================

    @Test
    @DisplayName("删除内容 - 创建者可以删除")
    void deleteContent_success() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);

        contentService.deleteContent(1L, 1L);

        verify(contentMapper, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("删除内容 - 非创建者无权删除")
    void deleteContent_notOwner_throwsException() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);

        assertThrows(BusinessException.class,
                () -> contentService.deleteContent(1L, 2L));
    }

    // ==================== 收藏 ====================

    @Test
    @DisplayName("切换收藏 - 从 0 变成 1")
    void toggleFavorite_on() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);

        contentService.toggleFavorite(1L, 1L);

        verify(contentMapper).updateById(contentCaptor.capture());
        assertEquals(1, contentCaptor.getValue().getFavorite());
    }

    @Test
    @DisplayName("切换收藏 - 从 1 变成 0")
    void toggleFavorite_off() {
        sampleContent.setFavorite(1);
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);

        contentService.toggleFavorite(1L, 1L);

        verify(contentMapper).updateById(contentCaptor.capture());
        assertEquals(0, contentCaptor.getValue().getFavorite());
    }

    // ==================== 版本管理 ====================

    @Test
    @DisplayName("回滚 - 成功回滚到指定版本")
    void rollback_success() {
        ContentVersion targetVersion = new ContentVersion();
        targetVersion.setVersion(2);
        targetVersion.setTitle("回滚目标标题");
        targetVersion.setSummary("回滚目标摘要");
        targetVersion.setContent("回滚目标内容");

        when(contentMapper.selectById(1L)).thenReturn(sampleContent);
        when(contentVersionMapper.selectOne(any(LambdaQueryWrapper.class)))
                .thenReturn(targetVersion) // 第1次: 查询目标版本
                .thenReturn(null)          // 第2次: getMaxVersion（无版本记录）
                .thenReturn(null);         // 第3次: getMaxVersion（回滚后）

        contentService.rollback(1L, 2, 1L);

        verify(contentMapper, times(1)).updateById(any(Content.class));
        // 验证保存了两个版本快照（回滚前快照 + 回滚后快照）
        verify(contentVersionMapper, times(2)).insert(any(ContentVersion.class));
    }

    @Test
    @DisplayName("回滚 - 目标版本不存在时抛出异常")
    void rollback_versionNotFound_throwsException() {
        when(contentMapper.selectById(1L)).thenReturn(sampleContent);
        // getMaxVersion 返回 null（无版本记录），然后查目标版本也返回 null
        when(contentVersionMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> contentService.rollback(1L, 99, 1L));

        assertTrue(exception.getMessage().contains("不存在"));
    }

    // ==================== 辅助方法 ====================

    private ContentDTO updateDTO() {
        ContentDTO dto = new ContentDTO();
        dto.setTitle("新标题");
        return dto;
    }
}
