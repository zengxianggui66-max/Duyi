package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏列表项（含资源快照）
 */
@Data
public class CollectItemVO {

    private Long id;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String stage;
    private String stageKey;
    private String subject;
    private String subjectKey;
    private String module;
    private String teachingType;
    private String gradeName;
    private String fileExt;
    private String ossUrl;
    private LocalDateTime collectTime;
}
