package com.k12.common.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;

/**
 * 资源上传DTO
 */
@Data
public class ResourceUploadDTO {
    private String title;             // 资源标题
    private String description;      // 资源描述
    private String gradeLevel;       // 学段: primary/junior/senior/art/dance
    private String subject;          // 学科
    private String grade;            // 年级
    private String version;          // 版本
    private String resourceType;     // 资源类型
    private String examType;         // 备考类型
    private Long categoryId;         // 分类ID
    private String tags;             // 标签(逗号分隔)
    private Integer isFree;          // 是否免费: 0-付费 1-免费
    private BigDecimal price;        // 价格
    private MultipartFile file;      // 上传的文件
    private Long authorId;           // 作者ID（由网关透传）
    private String authorName;       // 作者名称（由网关透传）
}
