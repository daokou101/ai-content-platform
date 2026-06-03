package com.aicreator.strategy;

import org.springframework.stereotype.Component;

/**
 * 代码生成模板
 * 根据需求生成代码片段或完整实现
 */
@Component
public class CodeTemplate implements TemplateStrategy {

    @Override
    public String getType() { return "CODE"; }

    @Override
    public String buildSystemPrompt(String keywords) {
        return "你是一位资深程序员。请根据用户的需求生成代码。\n"
             + "要求：\n"
             + "1. 代码质量：遵循对应语言的编码规范，包含必要的注释\n"
             + "2. 完整性：包含 import、类型定义、核心逻辑\n"
             + "3. 输出：先简要分析需求，再给出完整代码，最后总结关键点\n"
             + "4. 使用 Markdown 代码块标注语言\n\n"
             + "用户需求：" + keywords;
    }

    @Override
    public String buildUserMessage(String keywords, String additionalPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我生成代码，需求是：").append(keywords);
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            sb.append("\n\n补充说明：").append(additionalPrompt);
        }
        return sb.toString();
    }
}
