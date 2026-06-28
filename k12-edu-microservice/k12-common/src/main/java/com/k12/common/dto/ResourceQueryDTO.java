package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceQueryDTO {
    private String gradeLevel;
    private String subject;
    private String grade;
    private String version;
    private String resourceType;
    private String examType;
    private String keyword;
    private Integer current = 1;
    private Integer size = 15;
    private String sortField = "createTime";
    private String sortOrder = "desc";
    
    // 额外筛选条件
    private Long categoryId;  // 分类ID
    private Integer status;   // 状态
    
    // 媒体类型筛选（video, audio, document, image）
    private String mediaType;
    
    // 频道类型（banhui, shengya, chuantong, jingsai, zhuanti）
    private String channelType;

    /** 是否免费：1 免费 0 会员/付费 */
    private Integer isFree;
    
    // 前端兼容：支持 textbookVersion 参数（映射到 version）
    private String textbookVersion;
    
    // 兼容处理：返回 version 时优先使用 this.version
    public String getVersion() {
        return this.version != null ? this.version : this.textbookVersion;
    }
}
