package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminSearchSynonymWriteDTO {
    @NotBlank(message = "标准词不能为空")
    private String word;
    @NotBlank(message = "同义词不能为空")
    private String synonyms;
    private String domain = "global";
    private String canonical;
    private Integer status = 1;
}
