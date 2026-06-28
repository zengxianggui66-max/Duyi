package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminTaxonomyResourceTypeWriteDTO {

    @NotNull(message = "父级不能为空")
    private Integer parentId;

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String icon;
    private String groupCode;
    private String groupName;
    private Integer allowPreview;
    private Integer sort;
    private Integer status;
}
