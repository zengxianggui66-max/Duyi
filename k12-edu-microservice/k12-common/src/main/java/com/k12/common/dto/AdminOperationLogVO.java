package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogVO {
    private Long id;
    private Long userId;
    private String username;
    private String module;
    private String action;
    private String permission;
    private String requestUri;
    private String requestMethod;
    private Integer status;
    private String errorMsg;
    private Integer durationMs;
    private LocalDateTime createTime;
}
