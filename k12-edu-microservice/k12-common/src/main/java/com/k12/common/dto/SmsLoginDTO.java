package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机验证码登录 DTO
 */
@Data
public class SmsLoginDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "验证码为4-6位数字")
    private String code;

    /** 登录身份（已废弃；仅新用户自动注册时默认 teacher，已有账号登录不再校验） */
    @Pattern(regexp = "^(teacher|student|parent)?$", message = "身份须为教师、学生或家长")
    private String role;
}
