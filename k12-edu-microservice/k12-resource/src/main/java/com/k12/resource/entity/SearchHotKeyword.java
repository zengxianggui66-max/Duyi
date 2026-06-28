package com.k12.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 热搜关键词实体
 */
@Data
@TableName("search_hot_keyword")
public class SearchHotKeyword {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String keyword;
    private Integer searchCount;
    /** 运营人工加权，参与 C 端热搜排序 */
    private Integer boostScore;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
