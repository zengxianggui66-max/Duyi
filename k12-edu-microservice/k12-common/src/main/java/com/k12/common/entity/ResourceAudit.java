package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资源审核记录实体
 */
@Data
@TableName("resource_audit")
public class ResourceAudit {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;
    
    private Integer status;   // 0-待审核, 1-通过, 2-驳回
    
    private String reason;    // 审核意见
    
    private Long auditorId;  // 审核人ID
    
    private String auditorName;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
