package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSearchHotKeywordVO {
    private Long id;
    private String keyword;
    private Integer searchCount;
    private Integer boostScore;
    /** 按有效热度（searchCount + boostScore）计算的排名 */
    private Integer rank;
    private Integer status;
    private String updateTime;
}
