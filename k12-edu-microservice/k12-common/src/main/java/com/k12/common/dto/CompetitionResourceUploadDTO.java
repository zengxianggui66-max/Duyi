package com.k12.common.dto;

import lombok.Data;

/**
 * 竞赛专区资源上传
 */
@Data
public class CompetitionResourceUploadDTO {
    private String title;
    private String summary;
    private String category;
    private String gradeStage;
    private String subject;
    private String resourceForm;
    private String competitionName;
    private String tags;
    private String icon;
    private Integer isFree;
}
