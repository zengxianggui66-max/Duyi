package com.k12.common.dto;

import com.k12.common.entity.EduGrade;
import com.k12.common.entity.EduStage;
import com.k12.common.entity.EduVolume;
import lombok.Data;

/**
 * 教材册别（C 端 taxonomy 读模型，展示名如「一年级上册」）
 */
@Data
public class TaxonomyVolumeVO {

    private Integer id;
    /** 业务编码，如 y1s1 / grade1_up */
    private String code;
    /** 展示名（上传 gradeName 字段） */
    private String name;
    private Integer stageId;
    private String stageCode;
    private String stageName;
    private Integer gradeId;
    private String gradeCode;
    /** 年级部分，如「一年级」 */
    private String gradeName;
    private Integer volumeId;
    private String volumeCode;
    /** 册别部分，如「上册」 */
    private String volumePart;
    private Integer sort;

    public static TaxonomyVolumeVO fromParts(
            EduStage stage,
            EduGrade grade,
            EduVolume volume,
            String displayGradeName,
            String displayName,
            String volumeCode,
            int sort) {
        TaxonomyVolumeVO vo = new TaxonomyVolumeVO();
        vo.setId(grade.getId() * 10 + volume.getId());
        vo.setCode(volumeCode);
        vo.setName(displayName);
        if (stage != null) {
            vo.setStageId(stage.getId());
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
        }
        vo.setGradeId(grade.getId());
        vo.setGradeCode(grade.getCode());
        vo.setGradeName(displayGradeName);
        vo.setVolumeId(volume.getId());
        vo.setVolumeCode(volume.getCode());
        vo.setVolumePart(volume.getName());
        vo.setSort(sort);
        return vo;
    }
}
