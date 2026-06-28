package com.k12.common.dto;

import lombok.Data;

/**
 * 小学语文学科资源查询DTO
 * 对应表：xinketang.oss_primary_chinese_resource
 */
@Data
public class PrimaryChineseResourceQueryDTO {

    /**
     * 学段：小学/初中/高中
     */
    private String stage;

    /**
     * 学科：语文/数学/英语等
     */
    private String subject;

    /**
     * 资源栏目：同步备课/一轮复习/专题/月考/期中/期末/真题/模拟等
     */
    private String module;

    /**
     * 类型：教案/课件/练习/试卷/学案等
     */
    private String type;

    /**
     * 年级：一年级上册/一年级下册/二年级上册 等
     */
    private String gradeName;

    /**
     * 版本：人教版/统编版/北师大版等
     */
    private String edition;

    /**
     * 品牌编码：qicai | zhuangyuan | platform；不传则不过滤
     */
    private String brandCode;

    /**
     * 子类型：教案版/精品版/希沃等
     */
    private String subType;

    /**
     * 目录节点 ID（M3：优先 placement 查询）
     */
    private Long catalogNodeId;

    /**
     * 目录方案 ID（可选）
     */
    private Integer schemeId;

    /**
     * 展示类型 Tab 名（可选；若传则覆盖 type/subType 解析，与 stats 分组一致）
     */
    private String displayType;

    /**
     * 是否包含子树节点资源，默认 true（服务端会按 nodeType 校正）
     */
    private Boolean includeSubtree = true;

    /**
     * 单元/章节名称
     */
    private String unitName;

    /**
     * 课文名称（精确匹配，与列表课文叶子筛选一致）
     */
    private String lessonName;

    /**
     * 上传者用户 ID（「我的资源」按上传人筛选）
     */
    private Long uploaderId;

    /**
     * 标题关键词（模糊搜索）
     */
    private String keyword;

    /**
     * 文件扩展名筛选：ppt/doc/pdf/rar 等（精确匹配）
     */
    private String fileExt;

    /**
     * 状态筛选：-1=草稿 0=待审核 1=已发布 2=审核不通过 3=下架
     * 公开列表（无 uploaderId）不传时服务端默认 status=1；
     * 「我的资源」传 uploaderId 时不传 status 则查全部非草稿状态（status>=0 或按 Tab 传值）
     */
    private Integer status;

    /**
     * 审核状态筛选：-1 草稿 / 0 待审核 / 1 审核通过 / 2 驳回
     */
    private Integer auditStatus;

    /**
     * 发布状态筛选：0 未上架 / 1 已上架 / 2 已下架 / 4 回收站
     */
    private Integer publishStatus;

    /**
     * 当前页，默认第1页
     */
    private Integer current = 1;

    /**
     * 每页数量，默认15条
     */
    private Integer size = 15;

    /**
     * 排序字段：upload_time / file_size_kb / download_count / view_count / sort
     * 默认：upload_time
     */
    private String sortField = "upload_time";

    /**
     * 排序方向：asc / desc，默认 desc
     */
    private String sortOrder = "desc";
}
