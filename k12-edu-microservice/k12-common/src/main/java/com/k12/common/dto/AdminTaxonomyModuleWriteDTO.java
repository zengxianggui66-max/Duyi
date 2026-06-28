package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminTaxonomyModuleWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String icon;
    private String moduleCategory;
    private String description;
    private Integer sort;
    private Integer status;
    /** 适用学段 ID 列表（空=全学段） */
    private List<Integer> stageIds;
}
