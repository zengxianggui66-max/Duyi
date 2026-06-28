package com.k12.common.dto;

import lombok.Data;

@Data
public class TopicResourceUploadDTO {
    private String title;
    private String summary;
    private String category;
    private String region;
    private String gradeStage;
    private String subject;
    private String resourceForm;
    private String topicLabel;
    private String schoolYear;
    private String tags;
    private String icon;
    private Integer isFree;
}
