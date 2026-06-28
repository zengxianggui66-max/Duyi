package com.k12.common.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserDetailVO extends AdminUserListVO {

    private String email;
    private Integer gender;
    private LocalDate birthday;
    private LocalDateTime updateTime;

    private List<AdminOAuthBindVO> oauthBinds = new ArrayList<>();

    /** 摘要统计（详情页头部，可选由 resource 服务填充） */
    private Long uploadCount;
    private Long collectionCount;
}
