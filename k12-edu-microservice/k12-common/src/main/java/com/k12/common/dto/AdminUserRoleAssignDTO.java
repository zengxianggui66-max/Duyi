package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AdminUserRoleAssignDTO {
    private List<Long> roleIds = new ArrayList<>();
}
