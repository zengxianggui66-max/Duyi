package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpsChannelWriteDTO {
    @NotBlank
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
