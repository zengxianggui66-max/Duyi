package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminRoleCreateDTO {
    private String code;
    private String name;
    private String description;
    private Integer status = 1;
}
