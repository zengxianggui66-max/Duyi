package com.k12.common.dto;

import lombok.Data;

@Data
public class DuplicateResourceHintVO {
    private Long id;
    private String title;
    private Integer status;
    private String statusLabel;
    /** title | filename | oss_key */
    private String matchType;
}
