package com.k12.common.dto;

import com.k12.common.entity.EduCatalogScheme;
import lombok.Data;

/**
 * Phase 6：目录方案增加维度绑定展示字段
 */
@Data
public class CatalogSchemeVO {

    private Integer id;
    private String code;
    private String name;
    private Long brandId;
    private String brandCode;
    private String displayMode;
    private Integer sort;

    /** Phase 6：维度绑定展示 */
    private Integer stageId;
    private String stageName;
    private Integer subjectId;
    private String subjectName;
    private Integer editionId;
    private String editionName;
    private Integer volumeId;
    private String volumeName;
    private Integer status;

    public static CatalogSchemeVO fromEntity(EduCatalogScheme entity, String brandCode) {
        if (entity == null) {
            return null;
        }
        CatalogSchemeVO vo = new CatalogSchemeVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setBrandId(entity.getBrandId());
        vo.setBrandCode(brandCode);
        vo.setDisplayMode(entity.getDisplayMode());
        vo.setSort(entity.getSort());
        vo.setStageId(entity.getStageId());
        vo.setSubjectId(entity.getSubjectId());
        vo.setEditionId(entity.getEditionId());
        vo.setVolumeId(entity.getVolumeId());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
