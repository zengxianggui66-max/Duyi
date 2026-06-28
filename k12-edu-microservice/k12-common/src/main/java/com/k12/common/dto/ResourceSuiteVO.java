package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 成套资源包（按类型/单元聚合）
 */
@Data
public class ResourceSuiteVO {

    private String key;
    private String icon;
    private String title;
    private String sub;
    private String tag;
    private Integer fileCount;
    private String updateTime;
    private List<PrimaryChineseResourceVO> items = new ArrayList<>();
}
