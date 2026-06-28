package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSearchSynonymVO {
    private Long id;
    private String word;
    private String synonyms;
    private String domain;
    private String canonical;
    private Integer status;
    private String updateTime;
}
