package com.k12.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文件上传结果DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultDTO {
    private String fileName;         // 文件名
    private String fileUrl;          // 文件访问URL
    private String filePath;         // 存储路径
    private Long fileSize;           // 文件大小
    private String fileFormat;       // 文件格式
    private String contentType;      // MIME类型
    private Boolean isPreviewable;  // 是否可预览
    private String previewType;      // 预览类型
    private Long uploadRecordId;     // 上传记录ID
    private String message;          // 提示信息
}
