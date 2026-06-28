package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminMenuVO {
    private Long id;
    private Long parentId;
    private String title;
    private String path;
    private String name;
    private String icon;
    private String component;
    private String permissionCode;
    private Integer sort;
    private List<AdminMenuVO> children = new ArrayList<>();
}
