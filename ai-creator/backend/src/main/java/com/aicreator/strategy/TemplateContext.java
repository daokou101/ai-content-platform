package com.aicreator.strategy;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模板策略上下文
 *
 * 持有所有模板策略的实例，根据类型自动匹配对应的策略
 * 新增模板时只需创建一个类实现 TemplateStrategy 并加上 @Component，
 * Spring 会自动注入到此上下文，完全符合"开闭原则"——对扩展开放，对修改关闭
 *
 * @PostConstruct: Spring 生命周期注解，在依赖注入完成后自动执行
 *   用于初始化策略 Map，比在构造方法中初始化更安全（此时所有依赖已就绪）
 */
@Component
@RequiredArgsConstructor
public class TemplateContext {

    private final List<TemplateStrategy> strategies;
    private final Map<String, TemplateStrategy> strategyMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        for (TemplateStrategy s : strategies) {
            strategyMap.put(s.getType(), s);
        }
    }

    /**
     * 根据类型获取对应的模板策略
     * 如果没有匹配的类型，返回默认的 ARTICLE 策略
     */
    public TemplateStrategy getStrategy(String type) {
        return strategyMap.getOrDefault(type != null ? type.toUpperCase() : "ARTICLE",
                strategyMap.get("ARTICLE"));
    }

    /** 获取所有模板类型信息 */
    public List<Map<String, String>> getAllTypes() {
        return strategies.stream()
                .map(s -> Map.of("type", s.getType(), "name", getDisplayName(s.getType())))
                .toList();
    }

    private String getDisplayName(String type) {
        return switch (type) {
            case "ARTICLE" -> "文章创作";
            case "SOCIAL_MEDIA" -> "社交媒体文案";
            case "CODE" -> "代码生成";
            case "REPORT" -> "报告/周报";
            case "TRANSLATION" -> "翻译";
            default -> type;
        };
    }
}
