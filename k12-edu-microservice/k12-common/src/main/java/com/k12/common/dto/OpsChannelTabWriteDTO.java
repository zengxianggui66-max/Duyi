package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpsChannelTabWriteDTO {
    @NotBlank
    private String tabKey;
    @NotBlank
    private String tabName;
    private String icon;
    private String searchKeyword;
    private Integer sort;
    private Integer status;
}
