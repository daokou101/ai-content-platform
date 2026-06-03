package com.aicreator.strategy;

import org.springframework.stereotype.Component;

/**
 * 社交媒体文案模板
 * 生成小红书笔记、朋友圈文案、微博等短文案
 */
@Component
public class SocialMediaTemplate implements TemplateStrategy {

    @Override
    public String getType() { return "SOCIAL_MEDIA"; }

    @Override
    public String buildSystemPrompt(String keywords) {
        return "你是一位社交媒体文案创作专家。请根据用户提供的主题，创作吸引人的社交媒体内容。\n"
             + "要求：\n"
             + "1. 标题要抓人眼球，可使用emoji点缀\n"
             + "2. 内容结构：开头吸引注意 → 正文分享 → 结尾互动引导\n"
             + "3. 语言风格：亲切自然，像朋友之间的分享\n"
             + "4. 字数：200-500字\n"
             + "5. 可选：加入话题标签（#xxx）\n\n"
             + "用户主题：" + keywords;
    }

    @Override
    public String buildUserMessage(String keywords, String additionalPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我创作一篇关于「").append(keywords).append("」的社交媒体文案。");
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            sb.append("\n\n额外要求：").append(additionalPrompt);
        }
        return sb.toString();
    }
}
