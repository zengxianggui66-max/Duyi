package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomeFuncChannelWriteDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String examType;
    @NotBlank
    private String defaultTopic;
    @NotBlank
    private String stageKey;
    @NotBlank
    private String paperTab;
    @NotBlank
    private String paperDefaultGrade;
    private String scrollTarget;
    private String examTabLabel;
    private String browseModule;
    private String browseStageKey;
    private String browseDefaultVolume;
    private Integer sort;
    private Integer status;
}
