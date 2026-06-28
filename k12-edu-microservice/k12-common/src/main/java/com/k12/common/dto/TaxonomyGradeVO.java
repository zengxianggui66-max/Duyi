package com.k12.common.dto;

import com.k12.common.entity.EduGrade;
import com.k12.common.entity.EduStage;
import lombok.Data;

/**
 * 年级（C 端 taxonomy 读模型）
 */
@Data
public class TaxonomyGradeVO {

    private Integer id;
    private Integer stageId;
    private String stageCode;
    private String stageName;
    /** 年级编码，如 grade1 / grade7 */
    private String code;
    private String name;
    private Integer sort;
    private Integer status;

    public static TaxonomyGradeVO fromEntity(EduGrade grade, EduStage stage) {
        if (grade == null) {
            return null;
        }
        TaxonomyGradeVO vo = new TaxonomyGradeVO();
        vo.setId(grade.getId());
        vo.setStageId(grade.getStageId());
        vo.setCode(grade.getCode());
        vo.setName(grade.getName());
        vo.setSort(grade.getSort());
        vo.setStatus(grade.getStatus());
        if (stage != null) {
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
        }
        return vo;
    }
}
