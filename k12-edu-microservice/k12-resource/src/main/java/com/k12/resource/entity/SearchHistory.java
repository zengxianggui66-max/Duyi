package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 搜索历史实体
 */
@Data
@TableName("search_history")
public class SearchHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String keyword;
    private String searchType;  // resource-资源，course-课程
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
