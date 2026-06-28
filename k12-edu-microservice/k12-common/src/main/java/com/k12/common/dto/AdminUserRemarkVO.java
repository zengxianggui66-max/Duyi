package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserRemarkVO {

    private Long id;

    private Long userId;

    private Long adminUserId;

    private String adminUsername;

    private String content;

    private LocalDateTime createTime;
}
