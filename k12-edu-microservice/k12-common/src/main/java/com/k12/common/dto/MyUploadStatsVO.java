package com.k12.common.dto;

import lombok.Data;

/**
 * 我的上传统计
 */
@Data
public class MyUploadStatsVO {

    private Long total;
    private Long published;
    private Long pending;
    private Long draft;
    private Long rejected;
    private Long offline;
    private Long totalViews;
    private Long totalDownloads;
}
