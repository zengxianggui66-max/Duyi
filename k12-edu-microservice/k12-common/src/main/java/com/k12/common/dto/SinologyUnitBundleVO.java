package com.k12.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 国学阅读·作文深度融合——单元打包聚合 VO
 * 根据 unit_id 一次性返回该单元关联的：
 * - 国学阅读素材（通过 edu_resource_dimension 关联 module_id=34 国学阅读）
 * - 作文训练素材（通过 edu_resource_dimension 关联 module_id=14 作文）
 * - 单元基本信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinologyUnitBundleVO {

    /** 单元基本信息 */
    private UnitInfo unit;

    /** 国学阅读素材列表 */
    private List<SinologyCard> sinologyReadings;

    /** 作文训练素材列表（来自 edu_resource_dimension module_id=14） */
    private List<SinologyCard> compositionTrainings;

    /** 该单元对应的学校（地域维度，通过 edu_resource_region 反查） */
    private List<Map<String, Object>> relatedSchools;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UnitInfo {
        private Long unitId;
        private String unitName;
        private String gradeName;
        private String editionName;
        private String volumeName;
        private String semesterName;
        private String subjectName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SinologyCard {
        private Long readingId;
        private String title;
        private String dynasty;
        private String author;
        private String genre;
        private String content;
        private String translation;
        private String appreciation;
        private String compositionHint;
        private Integer difficulty;
        private String keyPhrases;
        private Integer wordCount;
        private String audioUrl;
        private String videoUrl;
    }
}
