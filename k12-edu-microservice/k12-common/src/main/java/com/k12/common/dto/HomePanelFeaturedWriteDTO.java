package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomePanelFeaturedWriteDTO {

    @NotBlank
    private String panelCode;

    @NotBlank
    private String tabKey;

    private String filterKey;

    private String stageKey;

    private String subjectName;

    private String gradeName;

    @NotNull
    private Long resourceId;

    @NotBlank
    private String resourceSource;

    private Integer sort;

    private LocalDateTime expireTime;

    private Integer status;
}
