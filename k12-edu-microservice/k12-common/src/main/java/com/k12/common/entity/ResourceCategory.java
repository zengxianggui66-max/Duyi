package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 资源分类实体（表 resource_category）
 *
 * @deprecated since Phase 3K — 表已废弃，row_count=0。冻结清单见
 *             {@code docs/Phase3K-表分层冻结清单.md}；DELETE 候选见
 *             {@code docs/Phase3K-delete-candidates.md}
 */
@Deprecated(since = "3K", forRemoval = true)
@Data
@TableName("resource_category")
public class ResourceCategory {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long parentId;            // 父分类ID
    private String name;             // 分类名称
    private String icon;             // 分类图标
    private Integer sortOrder;       // 排序
    private String type;             // 分类类型: doc/video/audio/image
    private Integer status;          // 状态: 0-禁用 1-启用
    @TableLogic
    private Integer deleted;         // 删除标记
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    // 非数据库字段
    private List<ResourceCategory> children;
}
