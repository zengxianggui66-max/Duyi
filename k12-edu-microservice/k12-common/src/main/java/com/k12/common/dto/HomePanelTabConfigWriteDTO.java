package com.k12.common.dto;

import lombok.Data;
import java.util.List;

@Data
public class HomePanelTabConfigWriteDTO {

    private String tabLabel;

    private List<String> moduleNames;

    private List<String> excludeModuleNames;

    private List<String> resourceTypeNames;

    private String titleKeyword;

    private String queryMode;

    private Integer sort;

    private Integer status;
}
