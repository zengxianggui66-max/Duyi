package com.k12.common.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AdminRoleVO {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Integer status;
    private Integer isBuiltin;
    private LocalDateTime createTime;
    private List<Long> permissionIds = new ArrayList<>();
}
