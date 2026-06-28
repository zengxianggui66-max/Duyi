package com.k12.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 文件预览信息（阶段 C：服务端转码 / POI 兜底 / 原生直链）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FilePreviewInfoDTO {
    /** 实际用于 iframe / 播放器的 URL */
    private String previewUrl;
    /** document | video | audio | image */
    private String previewType;
    /** native | pdf | html | slides | embed | none | processing */
    private String previewMode;
    /** 原始文件可访问 URL */
    private String originalUrl;
    /** 是否经过转码生成预览文件 */
    private Boolean converted;
    /** native | libreoffice | poi | none */
    private String provider;
    private String message;
    /** PPT 逐页预览图 URL（阶段 D） */
    private List<String> slideUrls;
    /** 幻灯片页数 */
    private Integer slideCount;
}
