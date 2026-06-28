package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 目录树节点（支持多层 children）
 */
@Data
public class CatalogNodeVO {

    private Long id;
    private String code;
    private String name;
    private String namePath;
    private Integer depth;
    private String nodeType;
    private String icon;
    private Map<String, Object> meta;
    private List<CatalogNodeVO> children = new ArrayList<>();

    /** 兼容旧版单元树：课文名列表 */
    private List<String> subUnits = new ArrayList<>();
}
