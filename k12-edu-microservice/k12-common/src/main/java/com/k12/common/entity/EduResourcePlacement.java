package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("edu_resource_placement")
public class EduResourcePlacement {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long resourceId;

    private Long catalogNodeId;

    private Integer isPrimary;

    private Integer moduleId;

    private Integer resourceTypeId;

    private Integer sort;
}
