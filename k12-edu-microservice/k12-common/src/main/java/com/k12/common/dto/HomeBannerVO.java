package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HomeBannerVO {

    private Long id;

    private String slotCode;

    private String title;

    private String subtitle;

    private String ctaText;

    private String icon;

    private String bgColorStart;

    private String bgColorEnd;

    private String imageUrl;

    private NavTargetDTO navTarget;

    private List<String> stageKeys;

    private Integer sort;

    /** Admin list only */
    private Integer status;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
