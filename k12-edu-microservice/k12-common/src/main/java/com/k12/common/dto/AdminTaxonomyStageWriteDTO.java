package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminTaxonomyStageWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String icon;
    private Integer sort;
    private Integer status;
}
