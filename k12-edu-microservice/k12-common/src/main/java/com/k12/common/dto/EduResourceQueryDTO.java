package com.k12.common.dto;

import lombok.Data;

/**
 * 教育资源查询DTO
 * 对应表：xinketang.edu_resource（通过 edu_resource_dimension 关联维度表）
 */
@Data
public class EduResourceQueryDTO {

    /**
     * 学段ID（可选）
     */
    private Integer stageId;

    /**
     * 学段名称（可选，优先于 stageId 使用）
     */
    private String stageName;

    /**
     * 学科ID（可选）
     */
    private Integer subjectId;

    /**
     * 学科名称（可选）
     */
    private String subjectName;

    /**
     * 版本ID（可选）
     */
    private Integer editionId;

    /**
     * 版本名称（可选，支持模糊匹配，如"统编版"可匹配"统编版(2024)"）
     */
    private String editionName;

    /**
     * 年级ID（可选）
     */
    private Integer gradeId;

    /**
     * 年级名称（可选，如"一年级"）
     */
    private String gradeName;

    /**
     * 学期ID（可选）
     */
    private Integer semesterId;

    /**
     * 学期名称（可选，如"上学期"/"下学期"）
     */
    private String semesterName;

    /**
     * 册别ID（可选）
     */
    private Integer volumeId;

    /**
     * 册别名称（可选，如"上册"/"下册"）
     */
    private String volumeName;

    /**
     * 栏目ID（可选）
     */
    private Integer moduleId;

    /**
     * 栏目名称（可选，如"同步备课"/"期中"/"期末"）
     */
    private String moduleName;

    /**
     * 资源类型ID（可选）
     */
    private Integer resourceTypeId;

    /**
     * 资源类型名称（可选，如"课件"/"教案"/"试卷"）
     */
    private String resourceTypeName;

    /**
     * 单元ID（可选）
     */
    private Long unitId;

    /**
     * 单元名称（可选，支持模糊匹配）
     */
    private String unitName;

    /**
     * 标题关键词（模糊搜索）
     */
    private String keyword;

    /**
     * 文件扩展名筛选（精确匹配，如 ppt/doc/pdf）
     */
    private String fileExt;

    /**
     * 状态筛选：0=待审核 1=已发布 2=审核不通过 3=下架
     * 不传时默认查已发布（status=1）
     */
    private Integer status;

    /**
     * 当前页，默认第1页
     */
    private Integer current = 1;

    /**
     * 每页数量，默认15条
     */
    private Integer size = 15;

    /**
     * 排序字段：upload_time / file_size_kb / download_count / view_count / collect_count / sort
     * 默认：upload_time
     */
    private String sortField = "upload_time";

    /**
     * 排序方向：asc / desc，默认 desc
     */
    private String sortOrder = "desc";
}
