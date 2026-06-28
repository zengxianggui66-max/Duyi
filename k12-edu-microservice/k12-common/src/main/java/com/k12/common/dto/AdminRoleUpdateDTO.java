package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminRoleUpdateDTO {
    private String name;
    private String description;
    private Integer status;
}
