package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** Phase 8-A：全站登录日志列表项 */
@Data
public class AdminSystemLoginLogVO {

    private Long id;
    private Long userId;
    private String username;
    private String loginType;
    private Integer success;
    private String failReason;
    private String ip;
    private String userAgent;
    private LocalDateTime createTime;
}
