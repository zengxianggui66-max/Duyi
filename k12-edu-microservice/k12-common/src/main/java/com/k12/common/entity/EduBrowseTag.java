package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览/上传资源属性标签（edu_browse_tag）
 */
@Data
@TableName("edu_browse_tag")
public class EduBrowseTag {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String code;

    private String name;

    private String tagGroup;

    /** JSON 数组字符串，如 ["art","dance"] */
    private String applicableStages;

    /** JSON 数组字符串，如 ["竞赛"] */
    private String applicableModules;

    private Integer sort;

    /** 0-停用 1-启用 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
