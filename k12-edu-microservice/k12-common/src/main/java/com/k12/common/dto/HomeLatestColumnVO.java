package com.k12.common.dto;

import lombok.Data;

@Data
public class HomeLatestColumnVO {

    private Long id;

    private String columnKey;

    private String title;

    private String morePath;

    /** rule | manual | api */
    private String dataSource;

    private HomeLatestRuleDTO rule;

    private Integer sort;

    private Integer status;

    private String remark;
}
