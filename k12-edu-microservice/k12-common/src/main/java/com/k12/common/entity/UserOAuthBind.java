package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户第三方账号绑定
 */
@Data
@TableName("user_oauth_bind")
public class UserOAuthBind {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String oauthType;
    private String oauthId;
    private String nickname;
    private String avatar;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
