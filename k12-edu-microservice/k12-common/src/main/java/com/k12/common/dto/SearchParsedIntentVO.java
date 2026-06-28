package com.k12.common.dto;

import lombok.Data;

/**
 * P2 搜索意图解析结果（供前端展示 / 调试）
 */
@Data
public class SearchParsedIntentVO {
    private String stage;
    private String subject;
    private String grade;
    private String resourceType;
    private String module;
    private String channel;
    private String contentDomain;
    private String normalizedQuery;
}
