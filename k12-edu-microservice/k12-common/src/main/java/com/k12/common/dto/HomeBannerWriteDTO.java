package com.k12.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HomeBannerWriteDTO {

    private String slotCode = "home_hero";

    @NotBlank
    private String title;

    private String subtitle;

    private String ctaText;

    private String icon;

    private String bgColorStart;

    private String bgColorEnd;

    private String imageUrl;

    @NotNull
    @Valid
    private NavTargetDTO navTarget;

    private List<String> stageKeys;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer sort;

    private Integer status;

    private String remark;
}
