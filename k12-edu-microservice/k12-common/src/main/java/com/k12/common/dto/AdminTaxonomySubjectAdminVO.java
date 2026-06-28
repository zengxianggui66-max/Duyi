package com.k12.common.dto;

import com.k12.common.entity.EduSubject;
import lombok.Data;

import java.util.List;

@Data
public class AdminTaxonomySubjectAdminVO {

    private Integer id;
    private Integer stageId;
    private String stageCode;
    private String stageName;
    private String code;
    private String name;
    private String icon;
    private Integer sort;
    private Integer status;
    private List<Integer> editionIds;
    private List<Integer> moduleIds;
    private List<Integer> resourceTypeIds;

    public static AdminTaxonomySubjectAdminVO from(
            EduSubject subject,
            String stageCode,
            String stageName,
            List<Integer> editionIds,
            List<Integer> moduleIds,
            List<Integer> resourceTypeIds) {
        AdminTaxonomySubjectAdminVO vo = new AdminTaxonomySubjectAdminVO();
        vo.setId(subject.getId());
        vo.setStageId(subject.getStageId());
        vo.setStageCode(stageCode);
        vo.setStageName(stageName);
        vo.setCode(subject.getCode());
        vo.setName(subject.getName());
        vo.setIcon(subject.getIcon());
        vo.setSort(subject.getSort());
        vo.setStatus(subject.getStatus());
        vo.setEditionIds(editionIds);
        vo.setModuleIds(moduleIds);
        vo.setResourceTypeIds(resourceTypeIds);
        return vo;
    }

    /** @deprecated use {@link #from(EduSubject, String, String, List, List, List)} */
    @Deprecated
    public static AdminTaxonomySubjectAdminVO from(EduSubject subject, String stageCode, String stageName, List<Integer> editionIds) {
        return from(subject, stageCode, stageName, editionIds, List.of(), List.of());
    }
}
