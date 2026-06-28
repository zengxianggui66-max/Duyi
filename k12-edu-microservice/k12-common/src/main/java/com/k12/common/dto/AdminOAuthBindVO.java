package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOAuthBindVO {

    private Long id;
    private String oauthType;
    private String oauthTypeName;
    private String oauthId;
    private String nickname;
    private String avatar;
    private LocalDateTime bindTime;
}
