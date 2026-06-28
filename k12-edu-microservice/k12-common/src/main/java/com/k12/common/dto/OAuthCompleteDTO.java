package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * OAuth 新用户补全（兼容旧流程，身份可选，默认 pending）
 */
@Data
public class OAuthCompleteDTO {
    @NotBlank(message = "缺少第三方类型")
    private String type;

    @NotBlank(message = "缺少第三方标识")
    private String oauthId;

    private String nickname;
    private String avatar;

    /** 可选，未传则 pending */
    @Pattern(regexp = "^(teacher|student|parent)?$", message = "身份须为教师、学生或家长")
    private String role;
}
