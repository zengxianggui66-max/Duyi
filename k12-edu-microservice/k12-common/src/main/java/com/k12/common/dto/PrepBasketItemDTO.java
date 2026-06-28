package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PrepBasketItemDTO {
    @NotBlank(message = "条目类型不能为空")
    private String itemType;
    @NotNull(message = "关联ID不能为空")
    private Long refId;
    private String title;
    private String subtitle;
    private String coverUrl;
    private String metaJson;
    private BigDecimal score;
}
