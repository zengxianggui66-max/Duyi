package com.k12.common.entity;



import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

import java.time.LocalDateTime;



/**

 * 收藏记录实体

 */

@Data

@TableName("collection")

public class Collection {

    @TableId(type = IdType.AUTO)

    private Long id;



    private Long userId;



    private Long resourceId;



    /**

     * 资源来源：resource | primary_chinese

     */

    private String resourceType;



    /** 资源标题快照 */

    private String title;



    /** 学段名称：小学/初中/高中/美术/舞蹈 */

    private String stage;



    /** 学段键：primary/junior/senior/art/dance */

    @TableField("stage_key")

    private String stageKey;



    /** 学科名称 */

    private String subject;



    @TableField("subject_key")

    private String subjectKey;



    /** 栏目 */

    private String module;



    /** 教学资源类型：课件/教案等 */

    @TableField("teaching_type")

    private String teachingType;



    @TableField("grade_name")

    private String gradeName;



    @TableField("file_ext")

    private String fileExt;



    @TableField("oss_url")

    private String ossUrl;



    @TableField(fill = FieldFill.INSERT)

    private LocalDateTime createTime;

}

