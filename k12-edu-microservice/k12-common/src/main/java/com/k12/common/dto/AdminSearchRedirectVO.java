package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSearchRedirectVO {
    private Long id;
    private String keyword;
    private String title;
    private String routePath;
    private NavTargetDTO navTarget;
    private Integer priority;
    private Integer status;
    private String remark;
    private String updateTime;
}
