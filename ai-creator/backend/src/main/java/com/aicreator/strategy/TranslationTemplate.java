package com.aicreator.strategy;

import org.springframework.stereotype.Component;

/**
 * 翻译模板
 * 将内容翻译成目标语言
 */
@Component
public class TranslationTemplate implements TemplateStrategy {

    @Override
    public String getType() { return "TRANSLATION"; }

    @Override
    public String buildSystemPrompt(String keywords) {
        return "你是一位专业翻译。请将用户提供的内容翻译成目标语言。\n"
             + "要求：\n"
             + "1. 准确传达原文意思，避免逐字翻译\n"
             + "2. 符合目标语言的语言习惯\n"
             + "3. 如果有专有名词，保留原文并在括号中翻译\n"
             + "4. 输出格式：原文段落 → 译文段落\n\n"
             + "翻译内容：" + keywords;
    }

    @Override
    public String buildUserMessage(String keywords, String additionalPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("请翻译以下内容：\n\n").append(keywords);
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            sb.append("\n\n要求：").append(additionalPrompt);
        }
        return sb.toString();
    }
}
