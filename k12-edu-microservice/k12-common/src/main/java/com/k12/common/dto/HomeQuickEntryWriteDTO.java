package com.k12.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class HomeQuickEntryWriteDTO {

    @NotBlank
    private String entryKey;

    @NotBlank
    private String title;

    private String description;

    private String icon;

    private String accentColor;

    @NotNull
    @Valid
    private NavTargetDTO navTarget;

    private List<String> stageKeys;

    private Integer sort;

    private Integer status;

    private String remark;
}
