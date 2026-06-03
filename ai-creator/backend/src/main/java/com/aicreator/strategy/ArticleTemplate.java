package com.aicreator.strategy;

import org.springframework.stereotype.Component;

/**
 * 文章创作模板
 * 用于生成微信公众号文章、博客、技术文章等长文
 */
@Component
public class ArticleTemplate implements TemplateStrategy {

    @Override
    public String getType() { return "ARTICLE"; }

    @Override
    public String buildSystemPrompt(String keywords) {
        return "你是一位专业的文章创作助手。请根据用户提供的主题，创作一篇高质量的文章。\n"
             + "要求：\n"
             + "1. 文章结构完整：包含引人入胜的开头、充实的主体内容和有力的结尾\n"
             + "2. 语言风格：专业但不晦涩，读起来流畅自然\n"
             + "3. 文章长度：1500-3000字\n"
             + "4. 使用 Markdown 格式输出，包含标题（## 级别）\n"
             + "5. 如果有数据或引用，请明确标注\n\n"
             + "用户主题：" + keywords;
    }

    @Override
    public String buildUserMessage(String keywords, String additionalPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我创作一篇关于「").append(keywords).append("」的文章。");
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            sb.append("\n\n额外要求：").append(additionalPrompt);
        }
        return sb.toString();
    }
}
