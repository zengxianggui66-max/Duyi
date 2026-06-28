package com.k12.common.dto;

import lombok.Data;

/**
 * 管理端主资源表（oss_primary_chinese_resource）分页查询
 */
@Data
public class AdminPrimaryResourceQueryDTO {

    private String stage;
    private String subject;
    private String module;
    private String type;
    private String gradeName;
    private String edition;
    private String keyword;
    /** 状态：-1 草稿 0 待审 1 已发布 2 驳回 3 下架 4 回收站；null 时默认排除回收站 */
    private Integer status;
    /** 审核状态：0 待审核 1 通过 2 驳回；待审队列优先使用此字段 */
    private Integer auditStatus;
    private Integer isRecommend;
    private Integer isTop;
    private Integer isFree;
    private Long uploaderId;

    private Integer current = 1;
    private Integer size = 15;
    private String sortField = "upload_time";
    private String sortOrder = "desc";
}
