package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminUserLoginLogVO {

    private Long id;
    private String loginType;
    private Integer success;
    private String failReason;
    private String ip;
    private LocalDateTime createTime;
}
