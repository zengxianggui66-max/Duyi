package com.k12.common.dto;

import lombok.Data;

/**
 * 传统文化研学资源上传
 */
@Data
public class CultureResourceUploadDTO {
    private String title;
    private String summary;
    /** guoxue|shici|calligraphy|festival|story|customs|bashu|yanxue */
    private String category;
    /** chengdu|bashu|sichuan|all */
    private String region;
    /** half_day|one_day|two_three_day|camp|flexible */
    private String durationType;
    private String durationLabel;
    private String suitableAudience;
    private String location;
    /** platform | external */
    private String resourceKind;
    private String externalUrl;
    private String sourceName;
    private String tags;
    private String icon;
    private Integer isFree;
}
