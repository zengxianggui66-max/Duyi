package com.k12.common.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /** 登录身份（已废弃，登录不再校验身份；新用户短信注册默认 teacher） */
    private String role;
}
