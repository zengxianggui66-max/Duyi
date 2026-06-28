package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminTaxonomyGradeWriteDTO {

    @NotNull(message = "学段不能为空")
    private Integer stageId;

    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private Integer sort;
    private Integer status;
}
