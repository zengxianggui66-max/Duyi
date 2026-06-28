package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminAnalyticsTopResourceVO {

    private Long id;
    private String title;
    private String stage;
    private String subject;
    private long downloadCount;
    private long viewCount;
    private long collectCount;
}
