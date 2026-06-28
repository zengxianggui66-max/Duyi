package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 地区（edu_region，省市区树）
 */
@Data
@TableName("edu_region")
public class EduRegion {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer parentId;

    private String code;

    private String name;

    /** 0全国 1省 2市 3区县 */
    private Integer level;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;
}
