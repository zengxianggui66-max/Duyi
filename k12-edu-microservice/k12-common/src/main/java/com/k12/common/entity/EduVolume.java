package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 册别维度（edu_volume：上册/下册/全册）
 * Phase 6：增加状态、多维绑定
 */
@Data
@TableName("edu_volume")
public class EduVolume {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    /** 绑定学段ID */
    @TableField("stage_id")
    private Integer stageId;

    /** 绑定学科ID */
    @TableField("subject_id")
    private Integer subjectId;

    /** 绑定版本ID */
    @TableField("edition_id")
    private Integer editionId;
}
