package com.k12.common.dto;

import lombok.Data;

/**
 * 首页专区列表项（仅标题为主，可选日期与跳转）
 */
@Data
public class HomePanelItemVO {

    private Long id;

    private String title;

    /** 展示日期 yyyy-MM-dd，可为空 */
    private String date;

    /** edu_resource | oss_primary_chinese | edu_resource_suite */
    private String source;

    private String detailPath;
}
