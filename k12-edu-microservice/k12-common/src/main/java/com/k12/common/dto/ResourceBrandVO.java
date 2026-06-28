package com.k12.common.dto;

import com.k12.common.entity.EduResourceBrand;
import lombok.Data;

@Data
public class ResourceBrandVO {

    private Long id;
    private String code;
    private String name;
    private String publisher;
    private String logoUrl;
    private Integer sort;

    public static ResourceBrandVO fromEntity(EduResourceBrand entity) {
        if (entity == null) {
            return null;
        }
        ResourceBrandVO vo = new ResourceBrandVO();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setPublisher(entity.getPublisher());
        vo.setLogoUrl(entity.getLogoUrl());
        vo.setSort(entity.getSort());
        return vo;
    }
}
