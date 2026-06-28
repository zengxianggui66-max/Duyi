package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserActionVO {

    private Long id;
    private String actionType;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String keyword;
    private Integer hitCount;
    private String ip;
    private String sourceApi;
    private LocalDateTime createTime;
}
