package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class AdminResourceBatchAuditDTO {

    private List<Long> ids;
    /** approve | reject */
    private String action;
    /** 驳回时必填 */
    private String reason;
}
