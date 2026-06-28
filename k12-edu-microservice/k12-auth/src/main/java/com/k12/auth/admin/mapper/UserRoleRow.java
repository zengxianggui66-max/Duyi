package com.k12.auth.admin.mapper;

import lombok.Data;

@Data
public class UserRoleRow {
    private Long userId;
    private String roleCode;
    private String roleName;
}
