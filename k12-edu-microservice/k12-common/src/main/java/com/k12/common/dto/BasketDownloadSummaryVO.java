package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class BasketDownloadSummaryVO {
    private int totalItems;
    private int downloadableCount;
    private int skippedCount;
    private List<String> skippedReasons;
}
