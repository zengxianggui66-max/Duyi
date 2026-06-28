package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class HomeHotWordVO {

    private Long id;

    private String label;

    private String actionType;

    private String badge;

    private NavTargetDTO navTarget;

    private List<String> stageKeys;

    private Integer sort;

    /** Admin list only */
    private Integer status;

    private String remark;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}
