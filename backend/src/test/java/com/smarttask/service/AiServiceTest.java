package com.smarttask.service;

import com.smarttask.entity.AiModel;
import com.smarttask.entity.AiTemplate;
import com.smarttask.mapper.AiModelMapper;
import com.smarttask.mapper.AiTemplateMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * AiService 单元测试
 *
 * 测试 AI 模板/模型查询、消息构建、SSE 生成编排等功能。
 */
@ExtendWith(MockitoExtension.class)
class AiServiceTest {

    @Mock
    private AiTemplateMapper aiTemplateMapper;

    @Mock
    private AiModelMapper aiModelMapper;

    @Mock
    private DeepSeekClient deepSeekClient;

    @InjectMocks
    private AiService aiService;

    private List<AiTemplate> mockTemplates;
    private List<AiModel> mockModels;

    @BeforeEach
    void setUp() {
        AiTemplate article = new AiTemplate();
        article.setType("article");
        article.setName("文章写作");
        article.setSystemPrompt("你是一位专业写手。");

        AiTemplate marketing = new AiTemplate();
        marketing.setType("marketing");
        marketing.setName("营销文案");
        marketing.setSystemPrompt("你是一位营销专家。");

        mockTemplates = List.of(article, marketing);

        AiModel chat = new AiModel();
        chat.setName("DeepSeek Chat");
        chat.setModel("deepseek-chat");

        AiModel reasoner = new AiModel();
        reasoner.setName("DeepSeek Reasoner");
        reasoner.setModel("deepseek-reasoner");

        mockModels = List.of(chat, reasoner);
    }

    // ==================== 模板/模型查询 ====================

    @Test
    @DisplayName("获取模板列表 - 返回排序后的模板")
    void getTemplates_returnsSortedList() {
        when(aiTemplateMapper.selectList(any())).thenReturn(mockTemplates);

        List<AiTemplate> result = aiService.getTemplates();

        assertEquals(2, result.size());
        assertEquals("article", result.get(0).getType());
    }

    @Test
    @DisplayName("获取模型列表 - 返回排序后的模型")
    void getModels_returnsSortedList() {
        when(aiModelMapper.selectList(any())).thenReturn(mockModels);

        List<AiModel> result = aiService.getModels();

        assertEquals(2, result.size());
        assertEquals("deepseek-chat", result.get(0).getModel());
    }

    @Test
    @DisplayName("模板列表为空 - 返回空列表")
    void getTemplates_emptyList() {
        when(aiTemplateMapper.selectList(any())).thenReturn(List.of());

        List<AiTemplate> result = aiService.getTemplates();

        assertTrue(result.isEmpty());
    }

    // ==================== SSE 流式生成 ====================

    @Test
    @DisplayName("generateStream - 模板不存在时发送错误事件")
    void generateStream_templateNotFound_sendsError() throws Exception {
        when(aiTemplateMapper.selectOne(any())).thenReturn(null);

        SseEmitter emitter = new SseEmitter();
        aiService.generateStream("test", "nonexistent", null, null, emitter);

        // 验证 emitter 发送了 error 事件（通过调用 emitter.complete() 会触发 CompletionCallback）
        // 由于 emitter.send() 和 emitter.complete() 已被调用，后续再发送会抛出异常
        assertThrows(Exception.class, () -> emitter.send(SseEmitter.event().name("test")));
    }

    @Test
    @DisplayName("generateStream - 成功调用 DeepSeek 流式 API")
    void generateStream_successful() throws Exception {
        when(aiTemplateMapper.selectOne(any())).thenReturn(mockTemplates.get(0));

        // 模拟 DeepSeekClient.streamChat 调用 onChunk 和 onComplete
        doAnswer(invocation -> {
            Consumer<String> onChunk = invocation.getArgument(1);
            Runnable onComplete = invocation.getArgument(2);
            onChunk.accept("你好");
            onChunk.accept("世界");
            onComplete.run();
            return null;
        }).when(deepSeekClient).streamChat(anyList(), any(), any(), any());

        SseEmitter emitter = new SseEmitter();
        aiService.generateStream("测试", "article", null, null, emitter);

        // 验证 emitter 完成了（onComplete 被调用）
        // 如果 emitter 已完成，send 会抛异常
        assertThrows(Exception.class, () -> emitter.send(SseEmitter.event().name("test")));
    }

    @Test
    @DisplayName("generateStream - DeepSeek 异常时发送错误事件")
    void generateStream_deepSeekError_sendsError() {
        when(aiTemplateMapper.selectOne(any())).thenReturn(mockTemplates.get(0));

        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(3);
            onError.accept(new RuntimeException("API 调用失败"));
            return null;
        }).when(deepSeekClient).streamChat(anyList(), any(), any(), any());

        SseEmitter emitter = new SseEmitter();
        aiService.generateStream("测试", "article", null, null, emitter);

        // 验证 emitter 已完成（onError 中调用了 complete）
        assertThrows(Exception.class, () -> emitter.send(SseEmitter.event().name("test")));
    }

    @Test
    @DisplayName("generateStream - 带有额外提示时构建正确的消息")
    void generateStream_withAdditionalPrompt() {
        when(aiTemplateMapper.selectOne(any())).thenReturn(mockTemplates.get(0));

        doAnswer(invocation -> {
            List<Map<String, String>> messages = invocation.getArgument(0);
            assertEquals(2, messages.size());

            // system message
            assertEquals("system", messages.get(0).get("role"));
            // user message 应包含额外提示
            String userContent = messages.get(1).get("content");
            assertTrue(userContent.contains("额外要求：语气轻松"));
            return null;
        }).when(deepSeekClient).streamChat(anyList(), any(), any(), any());

        SseEmitter emitter = new SseEmitter();
        aiService.generateStream("测试", "article", "语气轻松", null, emitter);
    }
}
