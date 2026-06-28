package com.k12.common.dto;

import com.k12.common.entity.EduEdition;
import com.k12.common.entity.EduSubject;
import lombok.Data;

/**
 * 教材版本（C 端 taxonomy 读模型）
 */
@Data
public class TaxonomyEditionVO {

    private Integer id;
    private String code;
    private String name;
    private String shortName;
    private String publisher;
    private String yearLabel;
    private Integer sort;
    private Integer status;
    private Integer subjectId;
    private String subjectCode;
    private String subjectName;

    public static TaxonomyEditionVO fromEntity(EduEdition edition, EduSubject subject) {
        TaxonomyEditionVO vo = new TaxonomyEditionVO();
        vo.setId(edition.getId());
        vo.setCode(edition.getCode());
        vo.setName(edition.getName());
        vo.setShortName(edition.getShortName());
        vo.setPublisher(edition.getPublisher());
        vo.setYearLabel(edition.getYearLabel());
        vo.setSort(edition.getSort());
        vo.setStatus(edition.getStatus());
        if (subject != null) {
            vo.setSubjectId(subject.getId());
            vo.setSubjectCode(subject.getCode());
            vo.setSubjectName(subject.getName());
        }
        return vo;
    }
}
