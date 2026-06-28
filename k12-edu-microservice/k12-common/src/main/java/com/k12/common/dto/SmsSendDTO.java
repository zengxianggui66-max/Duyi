package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 发送短信验证码 DTO
 */
@Data
public class SmsSendDTO {
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 用途: login/register/bind/set_password/change_phone_old/change_phone_new 等 */
    private String type = "login";

  private String captchaKey;

  private String captchaCode;
}
