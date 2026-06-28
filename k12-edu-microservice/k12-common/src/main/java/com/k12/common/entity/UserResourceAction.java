package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_resource_action")
public class UserResourceAction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String actionType;
    private Long resourceId;
    private String resourceType;
    private String title;
    private String keyword;
    private Integer hitCount;
    private String ip;
    private String userAgent;
    private String sourceApi;
    private LocalDateTime createTime;
}
