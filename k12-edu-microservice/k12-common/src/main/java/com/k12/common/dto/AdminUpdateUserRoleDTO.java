package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 管理员修改用户身份
 */
@Data
public class AdminUpdateUserRoleDTO {
    @NotBlank(message = "身份不能为空")
    @Pattern(regexp = "^(teacher|student|parent|pending)$", message = "身份须为教师、学生、家长或待分配")
    private String role;
}
