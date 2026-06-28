package com.k12.common.dto;

import com.k12.common.entity.EduResourceType;
import lombok.Data;

/**
 * 资源类型（C 端 taxonomy 读模型，叶子节点）
 */
@Data
public class TaxonomyResourceTypeVO {

    private Integer id;
    private Integer parentId;
    private String code;
    private String name;
    private String icon;
    private String groupCode;
    private String groupName;
    private Integer allowPreview;
    private Integer sort;
    private Integer status;

    public static TaxonomyResourceTypeVO fromEntity(EduResourceType type) {
        TaxonomyResourceTypeVO vo = new TaxonomyResourceTypeVO();
        vo.setId(type.getId());
        vo.setParentId(type.getParentId());
        vo.setCode(type.getCode());
        vo.setName(type.getName());
        vo.setIcon(type.getIcon());
        vo.setGroupCode(type.getGroupCode());
        vo.setGroupName(type.getGroupName());
        vo.setAllowPreview(type.getAllowPreview());
        vo.setSort(type.getSort());
        vo.setStatus(type.getStatus());
        return vo;
    }

    public static TaxonomyResourceTypeVO fromFallback(int id, String code, String name, int sort) {
        TaxonomyResourceTypeVO vo = new TaxonomyResourceTypeVO();
        vo.setId(id);
        vo.setCode(code);
        vo.setName(name);
        vo.setSort(sort);
        vo.setStatus(1);
        return vo;
    }
}
