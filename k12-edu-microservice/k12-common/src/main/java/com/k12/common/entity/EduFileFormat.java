package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 文件格式字典（edu_file_format）
 */
@Data
@TableName("edu_file_format")
public class EduFileFormat {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private String extensions;

    private String mimeTypes;

    private String previewType;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;
}
