package com.k12.common.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AdminUserBatchStatusDTO {

    @NotEmpty(message = "用户 ID 列表不能为空")
    private List<Long> ids;

    /** 1 启用 0 禁用 */
    @NotNull(message = "status 不能为空")
    private Integer status;
}
