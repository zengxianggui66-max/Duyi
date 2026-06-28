package com.k12.common.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度为3-20个字符")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度为6-32个字符")
    private String password;

    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    /** 用户身份（可选，未传则 pending 待管理员分配） */
    @Pattern(regexp = "^(teacher|student|parent)?$", message = "身份须为教师、学生或家长")
    private String role;

    @NotBlank(message = "请阅读并同意用户协议")
    private String agreedToTerms;
}
