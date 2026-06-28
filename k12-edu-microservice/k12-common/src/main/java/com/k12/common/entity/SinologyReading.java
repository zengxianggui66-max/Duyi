package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 国学阅读素材实体
 * 对应数据库表：xinketang.sinology_reading
 * 纯内容表，所有维度关联通过 edu_resource + edu_resource_dimension 实现
 */
@Data
@TableName("xinketang.sinology_reading")
public class SinologyReading {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联 edu_resource.id */
    @TableField("resource_id")
    private Long resourceId;

    /** 篇目标题 */
    private String title;

    /** 朝代 */
    private String dynasty;

    /** 作者/出处 */
    private String author;

    /** 出处典籍 */
    @TableField("source_book")
    private String sourceBook;

    /** 体裁：诗词/文言文/蒙学/经典/成语/寓言 */
    private String genre;

    /** 原文内容 */
    private String content;

    /** 白话译文 */
    private String translation;

    /** 赏析/写作手法分析 */
    private String appreciation;

    /** 作文启发：可关联的写作训练方向 */
    @TableField("composition_hint")
    private String compositionHint;

    /** 难度 1-5 */
    private Integer difficulty;

    /** 名句/成语，逗号分隔 */
    @TableField("key_phrases")
    private String keyPhrases;

    /** 朗读音频URL */
    @TableField("audio_url")
    private String audioUrl;

    /** 讲解视频URL */
    @TableField("video_url")
    private String videoUrl;

    /** 正文字数 */
    @TableField("word_count")
    private Integer wordCount;

    /** 状态：0=草稿 1=已发布 */
    private Integer status;

    private Integer sort;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ===== 以下为非数据库字段（JOIN 填充）=====

    /** 关联的单元名称 */
    @TableField(exist = false)
    private String unitName;

    /** 关联的单元ID */
    @TableField(exist = false)
    private Long unitId;

    /** 关联的年级名称 */
    @TableField(exist = false)
    private String gradeName;

    /** 关联的版本名称 */
    @TableField(exist = false)
    private String editionName;

    /** 关联的册别名 */
    @TableField(exist = false)
    private String volumeName;

    /** 关联的学期名 */
    @TableField(exist = false)
    private String semesterName;
}
