package com.k12.common.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdminUserUpdateDTO {

    private String nickname;
    private String email;
    private Integer gender;
    private LocalDate birthday;

    @Pattern(regexp = "^(teacher|student|parent|pending)$", message = "身份须为教师、学生、家长或待分配")
    private String portalRole;
}
