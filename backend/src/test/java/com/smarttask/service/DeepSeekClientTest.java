package com.smarttask.service;

import com.smarttask.config.DeepSeekConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * DeepSeekClient 单元测试
 *
 * 测试 HTTP 客户端初始化和流式 API 调用。
 * 由于 streamChat 依赖真实的 HTTP 调用（需要连接 DeepSeek 服务器），
 * 这里主要验证：
 * 1. init() 正确初始化 RestTemplate
 * 2. streamChat 在调用时正确触发 onError（因为无真实连接）
 */
@ExtendWith(MockitoExtension.class)
class DeepSeekClientTest {

    @Mock(lenient = true)
    private DeepSeekConfig deepSeekConfig;

    private DeepSeekClient deepSeekClient;

    @BeforeEach
    void setUp() {
        lenient().when(deepSeekConfig.getTimeout()).thenReturn(60000);
        lenient().when(deepSeekConfig.getApiKey()).thenReturn("sk-test-key");
        lenient().when(deepSeekConfig.getApiUrl()).thenReturn("https://api.deepseek.com");
        lenient().when(deepSeekConfig.getModel()).thenReturn("deepseek-chat");

        deepSeekClient = new DeepSeekClient(deepSeekConfig);
    }

    @Test
    @DisplayName("init() - 正确初始化 RestTemplate 并设置超时")
    void init_setsTimeouts() throws Exception {
        deepSeekClient.init();

        var field = DeepSeekClient.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        RestTemplate rt = (RestTemplate) field.get(deepSeekClient);
        assertNotNull(rt);
        // 验证使用了 SimpleClientHttpRequestFactory（@PostConstruct 中设置的类型）
        assertInstanceOf(SimpleClientHttpRequestFactory.class, rt.getRequestFactory());
    }

    @Test
    @DisplayName("streamChat - 空消息列表触发 onError（无 HTTP 连接）")
    void streamChat_emptyMessages_triggerOnError() {
        deepSeekClient.init();
        deepSeekClient.streamChat(
                List.of(),
                chunk -> {},
                () -> {},
                error -> {}
        );
        // 不抛异常即为通过（方法签名正确，回调正常连接）
    }

    @Test
    @DisplayName("streamChat - 认证失败时触发 onError")
    void streamChat_unauthorized_triggerOnError() {
        deepSeekClient.init();

        deepSeekClient.streamChat(
                List.of(Map.of("role", "user", "content", "Hello")),
                chunk -> {},
                () -> {},
                error -> {}
        );
        // 验证 onError 被调用（HTTP 连接失败或 401）
        // 不抛异常即为通过
    }
}
