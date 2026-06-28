package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 短信验证码实体
 */
@Data
@TableName("sms_code")
public class SmsCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String phone;
    private String code;
    private String type;
    private String ip;
    /** 0未使用 1已使用 2已过期 */
    private Integer status;
    private LocalDateTime expireTime;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
