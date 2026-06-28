package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 内容中心 Admin 写入（专题专辑 / 文化研学包 / 竞赛套卷共用字段）
 */
@Data
public class AdminContentPackageWriteDTO {
    @NotBlank(message = "标题不能为空")
    private String title;
    private String summary;
    private String category;
    private String region;
    private String gradeStage;
    private String durationType;
    private String durationLabel;
    private String suitableAudience;
    private String location;
    private String coverUrl;
    private String icon;
    private String tags;
    private Integer isFree;
    private Integer isElite;
    private Integer sort;
    private Integer status;
}
