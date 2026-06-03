package com.aicreator.strategy;

/**
 * 内容模板策略接口
 *
 * 策略模式（Strategy Pattern）：定义一系列算法，把它们一个个封装起来，并且使它们可以相互替换
 * 这里每种内容模板（文章、社交媒体、代码、报告、翻译）都是一种策略，
 * 各自实现自己的系统提示词构建逻辑
 *
 * 属性: type - 模板类型标识
 * 方法: buildSystemPrompt - 根据用户关键词构建 AI 系统提示词，返回 String
 *       从 Context（上下文）角度来看，这个提示词决定了 AI 的行为风格和输出格式
 */
public interface TemplateStrategy {

    /** 模板类型标识 */
    String getType();

    /**
     * 构建系统提示词
     * @param keywords 用户输入的关键词/主题
     * @return 完整的系统提示词
     */
    String buildSystemPrompt(String keywords);

    /**
     * 构建用户消息
     * @param keywords 用户输入的关键词
     * @param additionalPrompt 附加提示
     * @return 用户消息内容
     */
    String buildUserMessage(String keywords, String additionalPrompt);
}
