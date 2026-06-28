package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminPermissionVO {
    private Long id;
    private String code;
    private String name;
    private String type;
    private String module;
    private Long parentId;
    private Integer sort;
    private List<AdminPermissionVO> children = new ArrayList<>();
}
