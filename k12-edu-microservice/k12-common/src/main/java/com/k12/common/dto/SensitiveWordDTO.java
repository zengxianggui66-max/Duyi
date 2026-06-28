package com.k12.common.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Phase 8：敏感词增/改请求 DTO
 */
@Data
public class SensitiveWordDTO {

    @NotBlank(message = "敏感词不能为空")
    @Size(max = 100, message = "敏感词最多100个字符")
    private String word;

    /** 分类: 0-通用 1-政治 2-色情 3-广告 4-暴力 5-其他 */
    private Integer category;

    /** 严重级别: 1-警告 2-阻断 */
    private Integer level;

    /** 备注 */
    @Size(max = 200, message = "备注最多200个字符")
    private String remark;
}
