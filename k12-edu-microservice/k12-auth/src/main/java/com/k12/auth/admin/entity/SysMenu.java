package com.k12.auth.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sys_menu")
public class SysMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String title;
    private String path;
    private String name;
    private String icon;
    private String component;
    private String permissionCode;
    private Integer sort;
    private Integer hidden;
    private Integer status;
    private LocalDateTime createTime;
}
