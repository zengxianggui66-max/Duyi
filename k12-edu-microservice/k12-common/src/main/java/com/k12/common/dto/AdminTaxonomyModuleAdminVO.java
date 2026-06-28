package com.k12.common.dto;

import com.k12.common.entity.EduModule;
import lombok.Data;

import java.util.List;

@Data
public class AdminTaxonomyModuleAdminVO {

    private Integer id;
    private String code;
    private String name;
    private String icon;
    private String moduleCategory;
    private String description;
    private Integer sort;
    private Integer status;
    private List<Integer> stageIds;
    private List<String> stageNames;

    public static AdminTaxonomyModuleAdminVO from(EduModule module, List<Integer> stageIds, List<String> stageNames) {
        AdminTaxonomyModuleAdminVO vo = new AdminTaxonomyModuleAdminVO();
        vo.setId(module.getId());
        vo.setCode(module.getCode());
        vo.setName(module.getName());
        vo.setIcon(module.getIcon());
        vo.setModuleCategory(module.getModuleCategory());
        vo.setDescription(module.getDescription());
        vo.setSort(module.getSort());
        vo.setStatus(module.getStatus());
        vo.setStageIds(stageIds);
        vo.setStageNames(stageNames);
        return vo;
    }
}
