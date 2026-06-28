package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RolePermissionAssignDTO {
    private List<Long> permissionIds = new ArrayList<>();
}
