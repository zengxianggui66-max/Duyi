package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminCatalogNodeWriteDTO {

    @NotNull(message = "目录方案不能为空")
    private Integer schemeId;

    private Long parentId;

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "节点类型不能为空")
    private String nodeType;

    private Integer sort;
    private String icon;
    /** JSON 字符串，如 {"volumeKey":"y1s1","subject":"语文"} */
    private String meta;
    private Integer status;
}
