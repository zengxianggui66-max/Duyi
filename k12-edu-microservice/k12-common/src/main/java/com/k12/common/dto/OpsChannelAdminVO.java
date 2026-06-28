package com.k12.common.dto;

import lombok.Data;

@Data
public class OpsChannelAdminVO {
    private Long id;
    private String code;
    private String name;
    private String icon;
    private String description;
    private String bgGradient;
    private String routePath;
    private OpsChannelStatsDTO stats;
    private OpsChannelUiDTO ui;
    private Integer sort;
    private Integer status;
    private String remark;
}
