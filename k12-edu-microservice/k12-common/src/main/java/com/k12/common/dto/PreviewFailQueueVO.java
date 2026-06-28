package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 8：预览失败队列 VO
 */
@Data
public class PreviewFailQueueVO {

    private Long id;

    private String sourceType;

    private Long sourceId;

    private Long globalId;

    /** 资源标题 */
    private String title;

    /** 失败原因摘要 */
    private String failReason;

    /** 连续失败次数 */
    private Integer failCount;

    /** 最近失败时间 */
    private LocalDateTime lastFailTime;

    /** 0-待处理 1-已处理 2-已忽略 */
    private Integer status;

    /** 状态文本 */
    private String statusLabel;

    /** 处理人ID */
    private Long handlerId;

    /** 处理人姓名 */
    private String handlerName;

    /** 处理备注 */
    private String handlerNote;

    /** 处理时间 */
    private LocalDateTime handlerTime;

    private LocalDateTime createTime;

    public static String statusLabel(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "已处理";
            case 2: return "已忽略";
            default: return "未知";
        }
    }
}
