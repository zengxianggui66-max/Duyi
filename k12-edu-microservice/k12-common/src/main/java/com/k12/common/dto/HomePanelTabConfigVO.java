package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomePanelTabConfigVO {

    private Long id;

    private String panelCode;

    private String tabKey;

    private String filterKey;

    private String tabLabel;

    private List<String> moduleNames;

    private List<String> excludeModuleNames;

    private List<String> resourceTypeNames;

    private String titleKeyword;

    private String queryMode;

    private Integer sort;

    private Integer status;
}
