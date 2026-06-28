package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 手机号注册 DTO
 */
@Data
public class SmsRegisterDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @NotBlank(message = "验证码不能为空")
    @Pattern(regexp = "^\\d{4,6}$", message = "验证码为4-6位数字")
    private String code;

    /** 用户身份（可选，未传则 pending 待管理员分配） */
    @Pattern(regexp = "^(teacher|student|parent)?$", message = "身份须为教师、学生或家长")
    private String role;

    /** 昵称（可选，默认根据手机号生成） */
    private String nickname;

    @NotBlank(message = "请阅读并同意用户协议")
    private String agreedToTerms;
}
