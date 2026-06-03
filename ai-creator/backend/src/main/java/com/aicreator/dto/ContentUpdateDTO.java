package com.aicreator.dto;

import lombok.Data;

@Data
public class ContentUpdateDTO {
    private String title;
    private String content;
    private String summary;
    private Long categoryId;
    private String status;
    private String changeLog;    // 编辑时的变更说明
}
