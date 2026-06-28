package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 更换手机号（双验证：原手机 + 新手机）
 */
@Data
public class ChangePhoneDTO {

    @NotBlank(message = "新手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "新手机号格式不正确")
    private String newPhone;

    @NotBlank(message = "请输入原手机验证码")
    private String oldCode;

    @NotBlank(message = "请输入新手机验证码")
    private String newCode;
}
