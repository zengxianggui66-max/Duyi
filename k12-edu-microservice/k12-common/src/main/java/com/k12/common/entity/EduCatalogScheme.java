package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * Phase 6：增加多维分类绑定
 */
@Data
@TableName("edu_catalog_scheme")
public class EduCatalogScheme {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private Long brandId;

    /** 绑定学段ID */
    @TableField("stage_id")
    private Integer stageId;

    /** 绑定学科ID */
    @TableField("subject_id")
    private Integer subjectId;

    /** 绑定版本ID */
    @TableField("edition_id")
    private Integer editionId;

    /** 绑定册别ID */
    @TableField("volume_id")
    private Integer volumeId;

    private String displayMode;

    private String meta;

    private Integer sort;

    private Integer status;
}
