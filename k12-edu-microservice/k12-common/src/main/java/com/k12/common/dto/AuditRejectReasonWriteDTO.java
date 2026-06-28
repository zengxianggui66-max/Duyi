package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuditRejectReasonWriteDTO {
    @NotBlank(message = "驳回原因不能为空")
    @Size(max = 500, message = "驳回原因不超过500字")
    private String content;

    /** 问题分类: 0-通用, 1-内容质量, 2-格式规范, 3-版权合规, 4-分类挂载, 5-其他 */
    private Integer category;

    private Integer sort;

    /** 0-禁用 1-启用，默认 1 */
    private Integer status;
}
