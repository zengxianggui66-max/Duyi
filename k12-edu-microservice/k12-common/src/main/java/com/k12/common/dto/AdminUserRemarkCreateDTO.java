package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminUserRemarkCreateDTO {

    @NotBlank(message = "备注内容不能为空")
    @Size(max = 500, message = "备注最多 500 字")
    private String content;
}
