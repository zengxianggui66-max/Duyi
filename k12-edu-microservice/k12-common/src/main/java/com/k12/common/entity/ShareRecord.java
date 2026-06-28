package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 分享记录实体
 */
@Data
@TableName("share_record")
public class ShareRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long resourceId;

    /** resource | primary_chinese */
    private String resourceType;
    
    private Long userId;
    
    private String shareType;
    
    private String sharePlatform;
    
    private String ipAddress;
    
    private String userAgent;
    
    private String shareUrl;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
