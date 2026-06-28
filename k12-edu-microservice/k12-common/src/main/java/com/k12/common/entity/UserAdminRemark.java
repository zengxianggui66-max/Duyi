package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_admin_remark")
public class UserAdminRemark {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long adminUserId;

    private String adminUsername;

    private String content;

    private LocalDateTime createTime;
}
