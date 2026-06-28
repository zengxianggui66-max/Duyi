package com.k12.common.dto;

import lombok.Data;

import java.util.List;

/**
 * Rule config for home_latest_column when data_source=rule.
 */
@Data
public class HomeLatestRuleDTO {

    private String stageKey;

    private String subjectName;

    private List<String> moduleNames;

    private List<String> excludeModuleNames;

    private List<String> resourceTypeNames;

    private String titleKeyword;

    /** default 8 */
    private Integer limit;

    /** upload_time_desc */
    private String orderBy;
}
