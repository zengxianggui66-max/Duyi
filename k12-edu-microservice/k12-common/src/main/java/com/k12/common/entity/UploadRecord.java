package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文件上传记录实体
 */
@Data
@TableName("upload_record")
public class UploadRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;              // 上传用户ID
    private String fileName;          // 文件名
    private String filePath;          // 存储路径
    private Long fileSize;           // 文件大小(字节)
    private String fileFormat;       // 文件格式
    private String contentType;       // MIME类型
    private String status;           // 上传状态
    private String errorMessage;      // 错误信息
    private Long resourceId;          // 关联资源ID
    private String ipAddress;         // 上传IP
    private String deviceInfo;       // 设备信息
    @TableLogic
    private Integer deleted;          // 删除标记
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
