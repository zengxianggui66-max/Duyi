package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class HomeFuncChannelVO {

    private String key;
    private String name;
    private String examType;
    private String defaultTopic;
    private String stageKey;
    private String paperTab;
    private String paperDefaultGrade;
    private String scrollTarget;
    private String browseModule;
    private String browseStageKey;
    private String browseDefaultVolume;
    private List<String> topics = new ArrayList<>();
}
