package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminTaxonomyVolumeWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    /** 绑定学段ID */
    private Integer stageId;

    /** 绑定学科ID */
    private Integer subjectId;

    /** 绑定版本ID */
    private Integer editionId;
}
