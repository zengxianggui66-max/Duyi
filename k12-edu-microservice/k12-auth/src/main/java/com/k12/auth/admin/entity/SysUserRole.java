package com.k12.auth.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_user_role")
public class SysUserRole {
    private Long userId;
    private Long roleId;
}
