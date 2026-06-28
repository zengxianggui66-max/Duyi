package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Phase 8：预览失败队列
 */
@Data
@TableName("preview_fail_queue")
public class PreviewFailQueue {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 资源来源类型 */
    private String sourceType;

    /** 源资源ID */
    private Long sourceId;

    /** resource_main.id */
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

    /** 处理人ID */
    private Long handlerId;

    /** 处理人姓名 */
    private String handlerName;

    /** 处理备注 */
    private String handlerNote;

    /** 处理时间 */
    private LocalDateTime handlerTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
