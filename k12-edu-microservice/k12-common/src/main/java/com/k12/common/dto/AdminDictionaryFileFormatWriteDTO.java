package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminDictionaryFileFormatWriteDTO {

    @NotBlank(message = "编码不能为空")
    private String code;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "扩展名不能为空")
    private String extensions;

    private String mimeTypes;
    private String previewType;
    private Integer sort;
    private Integer status;
}
