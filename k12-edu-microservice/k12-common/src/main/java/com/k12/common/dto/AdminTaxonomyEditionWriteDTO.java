package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminTaxonomyEditionWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String shortName;

    /** 绑定学段ID（NULL=通用） */
    private Integer stageId;

    /** 绑定学科ID（NULL=通用） */
    private Integer subjectId;

    private String publisher;
    private String yearLabel;
    private Integer sort;
    private Integer status;
}
