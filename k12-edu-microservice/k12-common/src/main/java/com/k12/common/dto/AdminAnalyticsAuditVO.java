package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminAnalyticsAuditVO {

    private long approved;
    private long rejected;
    /** 0~100，无审核记录时为 null */
    private Double passRate;
}
