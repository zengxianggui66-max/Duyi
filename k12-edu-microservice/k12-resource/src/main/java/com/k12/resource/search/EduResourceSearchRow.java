package com.k12.resource.search;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EduResourceSearchRow {
    private Long resourceId;
    private String title;
    private String description;
    private String fileExt;
    private Integer downloadCount;
    private Integer viewCount;
    private LocalDateTime uploadTime;
    private String stageKey;
    private String stageName;
    private String subjectKey;
    private String subjectName;
    private String gradeName;
    private String editionName;
    private String moduleName;
    private String resourceTypeName;
    private String unitName;
}
