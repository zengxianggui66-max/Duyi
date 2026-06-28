package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改/设置登录密码
 * <ul>
 *   <li>原密码方式：填写 oldPassword + newPassword</li>
 *   <li>短信方式：已绑定手机时填写 phone + code + newPassword（phone 须与账号一致）</li>
 * </ul>
 */
@Data
public class ChangePasswordDTO {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "新密码长度为6-32个字符")
    private String newPassword;

    /** 原密码（与短信二选一） */
    private String oldPassword;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String code;
}
