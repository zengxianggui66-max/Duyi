package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminResourceAuditInsightsVO {
    private Long resourceId;
    private List<DuplicateResourceHintVO> duplicateHints = new ArrayList<>();
    private List<String> sensitiveWords = new ArrayList<>();
    /** safe | pending | risk | unknown */
    private String fileSafetyStatus = "unknown";
    private String fileSafetyMessage;
}
