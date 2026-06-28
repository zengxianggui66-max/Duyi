package com.k12.resource.service;

import com.k12.common.dto.FilePreviewInfoDTO;

/**
 * 文档预览解析（转码 / 兜底）
 */
public interface DocumentPreviewService {

    /**
     * 根据文件 URL 或相对路径解析最佳预览方式
     */
    FilePreviewInfoDTO resolvePreview(String fileUrl);
}
