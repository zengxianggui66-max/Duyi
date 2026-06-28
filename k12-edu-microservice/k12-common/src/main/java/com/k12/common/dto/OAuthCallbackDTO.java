package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * OAuth 扫码登录回调 DTO
 */
@Data
public class OAuthCallbackDTO {
    @NotBlank(message = "授权码不能为空")
    private String code;

    /** wechat 或 qq */
    @NotBlank(message = "登录类型不能为空")
    private String type;

    /** 前端回调的 state 参数（用于防 CSRF） */
    private String state;
}
