package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HomePanelFeaturedVO {

    private Long id;

    private String panelCode;

    private String tabKey;

    private String filterKey;

    private String stageKey;

    private String subjectName;

    private String gradeName;

    private Long resourceId;

    private String resourceSource;

    private String resourceTitle;

    private Integer sort;

    private LocalDateTime expireTime;

    private Integer status;
}
