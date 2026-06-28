package com.k12.common.dto;

import lombok.Data;

@Data
public class CultureQueryDTO {
    private String category;
    private String region;
    private String durationType;
    /** platform | external | 空=全部 */
    private String resourceKind;
    private String keyword;
    private Integer current = 1;
    private Integer size = 12;
    private String sortField = "sort";
    private String sortOrder = "desc";
}
