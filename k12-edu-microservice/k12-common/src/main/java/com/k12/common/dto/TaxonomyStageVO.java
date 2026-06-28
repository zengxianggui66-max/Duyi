package com.k12.common.dto;

import com.k12.common.entity.EduStage;
import lombok.Data;

/**
 * 学段（C 端 taxonomy 读模型）
 */
@Data
public class TaxonomyStageVO {

    private Integer id;
    /** 学段编码，如 primary / junior */
    private String code;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;

    public static TaxonomyStageVO fromEntity(EduStage stage) {
        TaxonomyStageVO vo = new TaxonomyStageVO();
        vo.setId(stage.getId());
        vo.setCode(stage.getCode());
        vo.setName(stage.getName());
        vo.setIcon(stage.getIcon());
        vo.setSort(stage.getSort());
        vo.setStatus(stage.getStatus());
        return vo;
    }
}
