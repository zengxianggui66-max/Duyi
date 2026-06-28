package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/** 管理端：用户上传资源摘要 */
@Data
public class AdminUserUploadVO {

    private Long id;
    private String title;
    private String stage;
    private String subject;
    private String module;
    private String type;
    private Integer status;
    private String statusLabel;
    private LocalDateTime uploadTime;
}
