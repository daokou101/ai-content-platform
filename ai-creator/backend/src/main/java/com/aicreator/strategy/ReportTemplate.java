package com.aicreator.strategy;

import org.springframework.stereotype.Component;

/**
 * 报告/周报生成模板
 * 生成工作周报、项目总结、数据分析报告等
 */
@Component
public class ReportTemplate implements TemplateStrategy {

    @Override
    public String getType() { return "REPORT"; }

    @Override
    public String buildSystemPrompt(String keywords) {
        return "你是一位专业的报告撰写助手。请根据用户提供的信息生成结构化的报告。\n"
             + "要求：\n"
             + "1. 报告结构：概述 → 详细内容 → 数据/成果 → 问题 → 下一步计划\n"
             + "2. 语言风格：正式、专业、简洁\n"
             + "3. 如果涉及数据，用表格呈现\n"
             + "4. 使用 Markdown 格式\n\n"
             + "用户主题：" + keywords;
    }

    @Override
    public String buildUserMessage(String keywords, String additionalPrompt) {
        StringBuilder sb = new StringBuilder();
        sb.append("请为我生成一份「").append(keywords).append("」报告。");
        if (additionalPrompt != null && !additionalPrompt.isEmpty()) {
            sb.append("\n\n额外内容：").append(additionalPrompt);
        }
        return sb.toString();
    }
}
