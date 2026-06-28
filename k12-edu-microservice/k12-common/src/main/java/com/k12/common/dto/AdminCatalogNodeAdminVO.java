package com.k12.common.dto;

import com.k12.common.entity.EduCatalogNode;
import lombok.Data;

@Data
public class AdminCatalogNodeAdminVO {

    private Long id;
    private Integer schemeId;
    private String schemeCode;
    private String schemeName;
    private Long parentId;
    private String parentName;
    private String code;
    private String name;
    private String namePath;
    private Integer depth;
    private String nodeType;
    private Integer sort;
    private String icon;
    private String meta;
    private Integer status;
    private Long childCount;

    public static AdminCatalogNodeAdminVO from(
            EduCatalogNode node,
            String schemeCode,
            String schemeName,
            String parentName,
            Long childCount) {
        AdminCatalogNodeAdminVO vo = new AdminCatalogNodeAdminVO();
        vo.setId(node.getId());
        vo.setSchemeId(node.getSchemeId());
        vo.setSchemeCode(schemeCode);
        vo.setSchemeName(schemeName);
        vo.setParentId(node.getParentId());
        vo.setParentName(parentName);
        vo.setCode(node.getCode());
        vo.setName(node.getName());
        vo.setNamePath(node.getNamePath());
        vo.setDepth(node.getDepth());
        vo.setNodeType(node.getNodeType());
        vo.setSort(node.getSort());
        vo.setIcon(node.getIcon());
        vo.setMeta(node.getMeta());
        vo.setStatus(node.getStatus());
        vo.setChildCount(childCount);
        return vo;
    }
}
