package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminSearchRedirectWriteDTO {
    @NotBlank(message = "关键词不能为空")
    private String keyword;
    private String title;
    private String routePath;
    private NavTargetDTO navTarget;
    private Integer priority = 0;
    private Integer status = 1;
    private String remark;
}
