package com.k12.common.dto;

import lombok.Data;

/**
 * 我的收藏分页查询
 */
@Data
public class CollectQueryDTO {

    /** 学段键：primary/junior/senior/art/dance */
    private String stageKey;

    /** 学科名称或学科键 */
    private String subject;

    /** 教学资源类型：课件/教案等；美术专区为 art_textbook 等 key */
    private String teachingType;

    private Integer current = 1;

    private Integer size = 20;
}
