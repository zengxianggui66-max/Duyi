package com.k12.common.dto;

import lombok.Data;

@Data
public class HomeFuncChannelAdminVO {
    private Long id;
    private String funcKey;
    private String name;
    private String examType;
    private String defaultTopic;
    private String stageKey;
    private String paperTab;
    private String paperDefaultGrade;
    private String scrollTarget;
    private String examTabLabel;
    private String browseModule;
    private String browseStageKey;
    private String browseDefaultVolume;
    private Integer sort;
    private Integer status;
}
