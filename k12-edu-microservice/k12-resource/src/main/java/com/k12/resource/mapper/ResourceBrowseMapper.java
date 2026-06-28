package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.BrowseModuleStatVO;
import com.k12.common.dto.BrowseTypeStatVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.util.ResourceDisplayType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源浏览查询（placement + 宽表 catalog_node_id + 单元课文 fallback 合并）
 */
@Mapper
public interface ResourceBrowseMapper {

    String CATALOG_MATCH_OR =
            " (" +
            "  EXISTS (SELECT 1 FROM edu_resource_placement p WHERE p.resource_id = r.id AND p.catalog_node_id IN " +
            "    <foreach collection='nodeIds' item='nid' open='(' separator=',' close=')'>#{nid}</foreach>)" +
            "  OR r.catalog_node_id IN " +
            "    <foreach collection='nodeIds' item='nid2' open='(' separator=',' close=')'>#{nid2}</foreach>" +
            "  <if test='catalogPathPrefix != null and catalogPathPrefix != \"\"'> " +
            "    OR r.catalog_path = #{catalogPathPrefix} OR r.catalog_path LIKE CONCAT(#{catalogPathPrefix}, '/%') " +
            "  </if>" +
            "  <if test='unitNames != null and unitNames.size() &gt; 0 and lessonNames != null and lessonNames.size() &gt; 0'> " +
            "    OR (r.unit_name IN " +
            "      <foreach collection='unitNames' item='u' open='(' separator=',' close=')'>#{u}</foreach>" +
            "      AND r.lesson_name IN " +
            "      <foreach collection='lessonNames' item='l' open='(' separator=',' close=')'>#{l}</foreach>)" +
            "  </if>" +
            "  <if test='unitNames != null and unitNames.size() &gt; 0 and (lessonNames == null or lessonNames.size() == 0)'> " +
            "    OR r.unit_name IN " +
            "      <foreach collection='unitNames' item='u2' open='(' separator=',' close=')'>#{u2}</foreach>" +
            "  </if>" +
            "  <if test='fallbackParentNodeId != null and titleKeywords != null and titleKeywords.size() &gt; 0'> " +
            "    OR (r.catalog_node_id = #{fallbackParentNodeId} AND (" +
            "      <foreach collection='titleKeywords' item='tk' separator=' OR '>r.title LIKE CONCAT('%', #{tk}, '%')</foreach>" +
            "    )) " +
            "  </if>" +
            " ) ";

    String TYPE_GROUP_EXPR = ResourceDisplayType.SQL_TYPE_GROUP_EXPR;

    String DIMENSION_FILTERS =
            "<if test='stage != null and stage != \"\"'> AND r.stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'> AND r.subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'> AND r.module = #{module} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND r.grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND r.edition = #{edition} </if>" +
            "<if test='brandCode != null and brandCode != \"\"'> " +
            "  AND (r.brand_code = #{brandCode} OR r.brand_code IS NULL OR TRIM(r.brand_code) = '') " +
            "</if>" +
            "<if test='unitName != null and unitName != \"\"'> AND r.unit_name = #{unitName} </if>" +
            "<if test='lessonName != null and lessonName != \"\"'> AND r.lesson_name = #{lessonName} </if>";

    String VISIBILITY_FILTERS =
            "<if test='auditStatus != null'> AND r.audit_status = #{auditStatus} </if>" +
            "<if test='publishStatus != null'> AND r.publish_status = #{publishStatus} </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='status == null and auditStatus == null and publishStatus == null'> AND r.status &gt;= 0 </if>";

    String COMMON_FILTERS =
            "<if test='stage != null and stage != \"\"'> AND r.stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'> AND r.subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'> AND r.module = #{module} </if>" +
            "<if test='type != null and type != \"\"'> AND r.type = #{type} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND r.grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND r.edition = #{edition} </if>" +
            "<if test='brandCode != null and brandCode != \"\"'> " +
            "  AND (r.brand_code = #{brandCode} OR r.brand_code IS NULL OR TRIM(r.brand_code) = '') " +
            "</if>" +
            "<if test='subType != null and subType != \"\"'> AND r.sub_type = #{subType} </if>" +
            VISIBILITY_FILTERS +
            "<if test='uploaderId != null'> AND r.uploader_id = #{uploaderId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  AND (r.title LIKE CONCAT('%', #{keyword}, '%') OR r.lesson_name LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>";

    @Select("<script>" +
            "SELECT DISTINCT r.* FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 AND " + CATALOG_MATCH_OR +
            COMMON_FILTERS +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<PrimaryChineseResource> findPageByCatalogUnified(
            Page<PrimaryChineseResource> page,
            @Param("nodeIds") List<Long> nodeIds,
            @Param("catalogPathPrefix") String catalogPathPrefix,
            @Param("unitNames") List<String> unitNames,
            @Param("lessonNames") List<String> lessonNames,
            @Param("fallbackParentNodeId") Long fallbackParentNodeId,
            @Param("titleKeywords") List<String> titleKeywords,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("subType") String subType,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    String STATS_VISIBILITY_FILTERS =
            "<if test='auditStatus != null'> AND r.audit_status = #{auditStatus} </if>" +
            "<if test='publishStatus != null'> AND r.publish_status = #{publishStatus} </if>" +
            "<if test='status != null'> AND r.status = #{status} </if>" +
            "<if test='status == null and auditStatus == null and publishStatus == null'> AND r.status &gt;= 0 </if>";

    @Select("<script>" +
            "SELECT " + TYPE_GROUP_EXPR + " AS displayType, COUNT(DISTINCT r.id) AS count " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 AND " + CATALOG_MATCH_OR +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "GROUP BY " + TYPE_GROUP_EXPR + " ORDER BY count DESC" +
            "</script>")
    List<BrowseTypeStatVO> statsByCatalogUnified(
            @Param("nodeIds") List<Long> nodeIds,
            @Param("catalogPathPrefix") String catalogPathPrefix,
            @Param("unitNames") List<String> unitNames,
            @Param("lessonNames") List<String> lessonNames,
            @Param("fallbackParentNodeId") Long fallbackParentNodeId,
            @Param("titleKeywords") List<String> titleKeywords,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    /** 与 findPageByCatalogUnified 同 scope 的总数（不含 type 筛选） */
    @Select("<script>" +
            "SELECT COUNT(DISTINCT r.id) " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 AND " + CATALOG_MATCH_OR +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "</script>")
    Long countByCatalogUnified(
            @Param("nodeIds") List<Long> nodeIds,
            @Param("catalogPathPrefix") String catalogPathPrefix,
            @Param("unitNames") List<String> unitNames,
            @Param("lessonNames") List<String> lessonNames,
            @Param("fallbackParentNodeId") Long fallbackParentNodeId,
            @Param("titleKeywords") List<String> titleKeywords,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    /** 无目录节点时，按学段/学科/册别/栏目等维度统计各类型数量 */
    @Select("<script>" +
            "SELECT " + TYPE_GROUP_EXPR + " AS displayType, COUNT(DISTINCT r.id) AS count " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 " +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "GROUP BY " + TYPE_GROUP_EXPR + " ORDER BY count DESC" +
            "</script>")
    List<BrowseTypeStatVO> statsByDimension(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("unitName") String unitName,
            @Param("lessonName") String lessonName,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    @Select("<script>" +
            "SELECT COUNT(DISTINCT r.id) " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 " +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "</script>")
    Long countByDimension(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("unitName") String unitName,
            @Param("lessonName") String lessonName,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    @Select("<script>" +
            "SELECT r.module AS module, COUNT(DISTINCT r.id) AS count " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 AND " + CATALOG_MATCH_OR +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "GROUP BY r.module ORDER BY count DESC" +
            "</script>")
    List<BrowseModuleStatVO> moduleStatsByCatalogUnified(
            @Param("nodeIds") List<Long> nodeIds,
            @Param("catalogPathPrefix") String catalogPathPrefix,
            @Param("unitNames") List<String> unitNames,
            @Param("lessonNames") List<String> lessonNames,
            @Param("fallbackParentNodeId") Long fallbackParentNodeId,
            @Param("titleKeywords") List<String> titleKeywords,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    @Select("<script>" +
            "SELECT r.module AS module, COUNT(DISTINCT r.id) AS count " +
            "FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 " +
            STATS_VISIBILITY_FILTERS +
            DIMENSION_FILTERS +
            "GROUP BY r.module ORDER BY count DESC" +
            "</script>")
    List<BrowseModuleStatVO> moduleStatsByDimension(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("unitName") String unitName,
            @Param("lessonName") String lessonName,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus
    );

    /** @deprecated 保留兼容，请使用 findPageByCatalogUnified */
    @Select("<script>" +
            "SELECT DISTINCT r.* FROM xinketang.oss_primary_chinese_resource r " +
            "INNER JOIN edu_resource_placement p ON p.resource_id = r.id " +
            "WHERE r.is_deleted = 0 " +
            "AND p.catalog_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            COMMON_FILTERS +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<PrimaryChineseResource> findPageByPlacement(
            Page<PrimaryChineseResource> page,
            @Param("nodeIds") List<Long> nodeIds,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("subType") String subType,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /** @deprecated 保留兼容，请使用 findPageByCatalogUnified */
    @Select("<script>" +
            "SELECT DISTINCT r.* FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 " +
            "<if test='catalogNodeId != null'> AND r.catalog_node_id = #{catalogNodeId} </if>" +
            "<if test='catalogPathPrefix != null and catalogPathPrefix != \"\"'> " +
            "  AND (r.catalog_path = #{catalogPathPrefix} OR r.catalog_path LIKE CONCAT(#{catalogPathPrefix}, '/%')) " +
            "</if>" +
            "<if test='unitNames != null and unitNames.size() &gt; 0'> AND r.unit_name IN " +
            "  <foreach collection='unitNames' item='u' open='(' separator=',' close=')'>#{u}</foreach> </if>" +
            "<if test='lessonNames != null and lessonNames.size() &gt; 0'> AND r.lesson_name IN " +
            "  <foreach collection='lessonNames' item='l' open='(' separator=',' close=')'>#{l}</foreach> </if>" +
            COMMON_FILTERS +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<PrimaryChineseResource> findPageByCatalogFallback(
            Page<PrimaryChineseResource> page,
            @Param("nodeIds") List<Long> nodeIds,
            @Param("catalogNodeId") Long catalogNodeId,
            @Param("catalogPathPrefix") String catalogPathPrefix,
            @Param("unitNames") List<String> unitNames,
            @Param("lessonNames") List<String> lessonNames,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("subType") String subType,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    @Select("<script>" +
            "SELECT r.type AS type, COUNT(DISTINCT r.id) AS count FROM xinketang.oss_primary_chinese_resource r " +
            "INNER JOIN edu_resource_placement p ON p.resource_id = r.id " +
            "WHERE r.is_deleted = 0 AND r.status &gt;= 0 " +
            "AND p.catalog_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "<if test='stage != null and stage != \"\"'> AND r.stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'> AND r.subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'> AND r.module = #{module} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND r.grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND r.edition = #{edition} </if>" +
            "<if test='brandCode != null and brandCode != \"\"'> " +
            "  AND (r.brand_code = #{brandCode} OR r.brand_code IS NULL OR TRIM(r.brand_code) = '') " +
            "</if>" +
            "GROUP BY r.type ORDER BY count DESC" +
            "</script>")
    List<BrowseTypeStatVO> statsByPlacement(
            @Param("nodeIds") List<Long> nodeIds,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode
    );
}
