package com.k12.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新个人资料
 */
@Data
public class UpdateProfileDTO {

    @Size(min = 1, max = 50, message = "昵称长度为1-50个字符")
    private String nickname;

    @Size(max = 500, message = "头像地址过长")
    private String avatar;

    @Min(value = 0, message = "性别取值无效")
    @Max(value = 2, message = "性别取值无效")
    private Integer gender;

    /** YYYY-MM-DD，空字符串表示清空 */
    private String birthday;

    @Email(message = "邮箱格式不正确")
    private String email;
}
