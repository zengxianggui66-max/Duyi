package com.k12.common.dto;

import lombok.Data;

/**
 * 单元文档查询DTO
 */
@Data
public class UnitDocQueryDTO {
    
    private Integer current = 1;     // 当前页
    private Integer size = 10;        // 每页大小
    
    private String unitName;          // 单元名称（模糊查询）
    private String originalFilename;   // 原始文件名（模糊查询）
    private String fileType;           // 文件类型（doc/docx/pdf等）
    private Integer minSizeKb;        // 最小文件大小（KB）
    private Integer maxSizeKb;        // 最大文件大小（KB）
    
    private String sortField = "uploadTime";  // 排序字段
    private String sortOrder = "desc";        // 排序方向（asc/desc）
    
    /**
     * 获取排序SQL
     */
    public String getOrderBySql() {
        String field = "upload_time";  // 默认排序字段
        if ("unitName".equals(sortField)) {
            field = "unit_name";
        } else if ("fileSize".equals(sortField)) {
            field = "file_size_kb";
        } else if ("uploadTime".equals(sortField)) {
            field = "upload_time";
        }
        
        String order = "desc".equalsIgnoreCase(sortOrder) ? "DESC" : "ASC";
        return field + " " + order;
    }
}
