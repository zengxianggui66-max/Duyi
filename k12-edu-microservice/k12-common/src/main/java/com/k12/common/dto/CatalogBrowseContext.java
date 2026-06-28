package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 由 catalog 节点解析出的浏览筛选上下文（M3 fallback）
 */
@Data
public class CatalogBrowseContext {

    private Long catalogNodeId;
    private String nodeType;
    private String namePath;
    private String catalogPathPrefix;
    private String defaultModule;
    private String defaultType;
    private String subType;
    private List<Long> nodeIds = new ArrayList<>();
    private List<String> unitNames = new ArrayList<>();
    private List<String> lessonNames = new ArrayList<>();
    /** 期末复习单元叶子：资源挂在父文件夹时，按标题关键词回退匹配 */
    private Long fallbackParentNodeId;
    private List<String> titleKeywords = new ArrayList<>();
    private Map<String, Object> meta;
}
