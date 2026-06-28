package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class AdminDictionaryBrowseTagWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    private String tagGroup;
    private List<String> applicableStages;
    private List<String> applicableModules;
    private Integer sort;
    private Integer status;
}
