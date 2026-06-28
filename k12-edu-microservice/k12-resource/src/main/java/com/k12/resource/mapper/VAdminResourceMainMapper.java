package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.entity.VAdminResourceMain;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Phase 7：统一资源视图 Mapper（v_admin_resource_main）
 * 管理端资源列表 / 审核中心 共用此查询层
 */
@Mapper
public interface VAdminResourceMainMapper extends BaseMapper<VAdminResourceMain> {

    /**
     * 管理端分页（排除已删除，支持多维筛选）
     */
    @Select("<script>" +
            "SELECT * FROM v_admin_resource_main WHERE is_deleted = 0 " +
            "<if test='query.sourceType != null and query.sourceType != \"\"'> AND source_type = #{query.sourceType} </if>" +
            "<if test='query.stage != null and query.stage != \"\"'> AND stage = #{query.stage} </if>" +
            "<if test='query.subject != null and query.subject != \"\"'> AND subject = #{query.subject} </if>" +
            "<if test='query.module != null and query.module != \"\"'> AND module = #{query.module} </if>" +
            "<if test='query.type != null and query.type != \"\"'> AND type = #{query.type} </if>" +
            "<if test='query.subType != null and query.subType != \"\"'> AND sub_type = #{query.subType} </if>" +
            "<if test='query.gradeName != null and query.gradeName != \"\"'> AND grade_name = #{query.gradeName} </if>" +
            "<if test='query.edition != null and query.edition != \"\"'> AND edition = #{query.edition} </if>" +
            "<if test='query.brandCode != null and query.brandCode != \"\"'> AND brand_code = #{query.brandCode} </if>" +
            "<if test='query.unitName != null and query.unitName != \"\"'> AND unit_name = #{query.unitName} </if>" +
            "<if test='query.lessonName != null and query.lessonName != \"\"'> AND lesson_name = #{query.lessonName} </if>" +
            "<if test='query.fileExt != null and query.fileExt != \"\"'> AND file_ext = #{query.fileExt} </if>" +
            "<if test='query.catalogNodeId != null and query.catalogNodeId &gt; 0 and query.includeSubtree != true'> AND catalog_node_id = #{query.catalogNodeId} </if>" +
            "<if test='query.catalogNodeId != null and query.catalogNodeId &gt; 0 and query.includeSubtree == true'> " +
            "  AND (catalog_node_id = #{query.catalogNodeId} OR catalog_path LIKE CONCAT((SELECT n.name_path FROM edu_catalog_node n WHERE n.id = #{query.catalogNodeId} LIMIT 1), '/%')) </if>" +
            "<if test='query.status != null'> AND legacy_status = #{query.status} </if>" +
            "<if test='query.status == null'> AND legacy_status != 4 </if>" +
            "<if test='query.auditStatus != null'> AND audit_status = #{query.auditStatus} </if>" +
            "<if test='query.publishStatus != null'> AND publish_status = #{query.publishStatus} </if>" +
            "<if test='query.isRecommend != null'> AND is_recommend = #{query.isRecommend} </if>" +
            "<if test='query.isTop != null'> AND is_top = #{query.isTop} </if>" +
            "<if test='query.isFree != null'> AND is_free = #{query.isFree} </if>" +
            "<if test='query.uploaderId != null'> AND uploader_id = #{query.uploaderId} </if>" +
            "<if test='query.keyword != null and query.keyword != \"\"'> " +
            "  AND (title LIKE CONCAT('%', #{query.keyword}, '%') OR lesson_name LIKE CONCAT('%', #{query.keyword}, '%')) </if>" +
            "ORDER BY ${query.sortField} ${query.sortOrder}" +
            "</script>")
    Page<VAdminResourceMain> findPage(Page<VAdminResourceMain> page, @Param("query") ResourceMainQueryDTO query);

    /**
     * 待审队列（audit_status = 0 且未删除）
     */
    @Select("<script>" +
            "SELECT * FROM v_admin_resource_main WHERE is_deleted = 0 AND audit_status = 0 " +
            "<if test='query.sourceType != null and query.sourceType != \"\"'> AND source_type = #{query.sourceType} </if>" +
            "<if test='query.stage != null and query.stage != \"\"'> AND stage = #{query.stage} </if>" +
            "<if test='query.subject != null and query.subject != \"\"'> AND subject = #{query.subject} </if>" +
            "<if test='query.keyword != null and query.keyword != \"\"'> " +
            "  AND (title LIKE CONCAT('%', #{query.keyword}, '%')) </if>" +
            "ORDER BY upload_time ASC" +
            "</script>")
    Page<VAdminResourceMain> findPendingPage(Page<VAdminResourceMain> page, @Param("query") ResourceMainQueryDTO query);

    /**
     * 按 global_id 查单条
     */
    @Select("SELECT * FROM v_admin_resource_main WHERE global_id = #{globalId} AND is_deleted = 0 LIMIT 1")
    VAdminResourceMain findByGlobalId(@Param("globalId") Long globalId);

    /**
     * 资源统计（按 source_type 分组）
     */
    @Select("<script>" +
            "SELECT source_type, COUNT(*) AS total, " +
            "  SUM(CASE WHEN audit_status = 0 THEN 1 ELSE 0 END) AS pending, " +
            "  SUM(CASE WHEN audit_status = 1 THEN 1 ELSE 0 END) AS approved, " +
            "  SUM(CASE WHEN publish_status = 1 THEN 1 ELSE 0 END) AS published, " +
            "  SUM(CASE WHEN publish_status = 4 THEN 1 ELSE 0 END) AS recycled " +
            "FROM v_admin_resource_main WHERE is_deleted = 0 " +
            "<if test='uploaderId != null'> AND uploader_id = #{uploaderId} </if>" +
            "GROUP BY source_type" +
            "</script>")
    List<Map<String, Object>> statsBySourceType(@Param("uploaderId") Long uploaderId);

    @Select("SELECT DISTINCT edition FROM v_admin_resource_main " +
            "WHERE source_type = #{sourceType} AND edition IS NOT NULL AND edition <> '' ORDER BY edition")
    List<String> findDistinctEditionsBySourceType(@Param("sourceType") String sourceType);

    @Select("SELECT DISTINCT grade_name FROM v_admin_resource_main " +
            "WHERE source_type = #{sourceType} AND grade_name IS NOT NULL AND grade_name <> '' ORDER BY grade_name")
    List<String> findDistinctGradesBySourceType(@Param("sourceType") String sourceType);
}
