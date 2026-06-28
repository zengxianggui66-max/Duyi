package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_login_log")
public class UserLoginLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    /** password / sms / oauth / admin */
    private String loginType;

    /** 1 成功 0 失败 */
    private Integer success;

    private String failReason;

    private String ip;

    private String userAgent;

    private LocalDateTime createTime;
}
