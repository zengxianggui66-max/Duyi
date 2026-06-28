package com.k12.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HomeHotWordWriteDTO {

    @NotBlank
    private String label;

    @NotBlank
    private String actionType;

    @NotNull
    @Valid
    private NavTargetDTO navTarget;

    private String badge;

    private List<String> stageKeys;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer sort;

    private Integer status;

    private String remark;
}
