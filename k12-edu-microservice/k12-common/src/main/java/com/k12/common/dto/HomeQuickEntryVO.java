package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeQuickEntryVO {

    private Long id;

    private String entryKey;

    private String title;

    private String description;

    private String icon;

    private String accentColor;

    private NavTargetDTO navTarget;

    private List<String> stageKeys;

    private Integer sort;

    /** Admin list only */
    private Integer status;

    private String remark;
}
