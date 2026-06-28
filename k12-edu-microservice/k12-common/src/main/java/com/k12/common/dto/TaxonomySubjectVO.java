package com.k12.common.dto;

import com.k12.common.entity.EduStage;
import com.k12.common.entity.EduSubject;
import lombok.Data;

/**
 * 学科（C 端 taxonomy 读模型）
 */
@Data
public class TaxonomySubjectVO {

    private Integer id;
    private Integer stageId;
    private String stageCode;
    private String stageName;
    /** 学科编码，如 chinese / math */
    private String code;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;

    public static TaxonomySubjectVO fromEntity(EduSubject subject, EduStage stage) {
        TaxonomySubjectVO vo = new TaxonomySubjectVO();
        vo.setId(subject.getId());
        vo.setStageId(subject.getStageId());
        vo.setCode(subject.getCode());
        vo.setName(subject.getName());
        vo.setIcon(subject.getIcon());
        vo.setSort(subject.getSort());
        vo.setStatus(subject.getStatus());
        if (stage != null) {
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
        }
        return vo;
    }
}
