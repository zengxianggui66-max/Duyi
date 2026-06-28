package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.PrimaryChineseResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 小学语文学科资源Mapper接口
 * 对应表：xinketang.oss_primary_chinese_resource
 */
@Mapper
public interface PrimaryChineseResourceMapper extends BaseMapper<PrimaryChineseResource> {

    /**
     * 根据多条件查询资源（不分页）
     */
    @Select("<script>" +
            "SELECT * FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 " +
            "<if test='stage != null and stage != \"\"'>   AND stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'>   AND subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'>   AND module = #{module} </if>" +
            "<if test='type != null and type != \"\"'>   AND type = #{type} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'>   AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'>   AND edition = #{edition} </if>" +
            "<if test='brandCode != null and brandCode != \"\"'>   AND brand_code = #{brandCode} </if>" +
            "<if test='subType != null and subType != \"\"'>   AND sub_type = #{subType} </if>" +
            "<if test='unitName != null and unitName != \"\"'>   AND unit_name = #{unitName} </if>" +
            "<if test='lessonName != null and lessonName != \"\"'>   AND lesson_name = #{lessonName} </if>" +
            "<if test='fileExt != null and fileExt != \"\"'>   AND file_ext = #{fileExt} </if>" +
            "<if test='status != null'>   AND status = #{status} </if>" +
            "<if test='status == null'>   AND status &gt;= 0 </if>" +
            "<if test='auditStatus != null'>   AND audit_status = #{auditStatus} </if>" +
            "<if test='publishStatus != null'>   AND publish_status = #{publishStatus} </if>" +
            "<if test='uploaderId != null'>   AND uploader_id = #{uploaderId} </if>" +
            "<if test='keyword != null and keyword != \"\"'>   AND (title LIKE CONCAT('%', #{keyword}, '%') OR lesson_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY sort DESC, upload_time DESC" +
            "</script>")
    List<PrimaryChineseResource> findByCondition(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("subType") String subType,
            @Param("unitName") String unitName,
            @Param("lessonName") String lessonName,
            @Param("fileExt") String fileExt,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword
    );

    /**
     * 根据多条件分页查询资源
     */
    @Select("<script>" +
            "SELECT * FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 " +
            "<if test='stage != null and stage != \"\"'>   AND stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'>   AND subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'>   AND module = #{module} </if>" +
            "<if test='type != null and type != \"\"'>   AND type = #{type} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'>   AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'>   AND edition = #{edition} </if>" +
            "<if test='brandCode != null and brandCode != \"\"'>   AND brand_code = #{brandCode} </if>" +
            "<if test='subType != null and subType != \"\"'>   AND sub_type = #{subType} </if>" +
            "<if test='unitName != null and unitName != \"\"'>   AND unit_name = #{unitName} </if>" +
            "<if test='lessonName != null and lessonName != \"\"'>   AND lesson_name = #{lessonName} </if>" +
            "<if test='fileExt != null and fileExt != \"\"'>   AND file_ext = #{fileExt} </if>" +
            "<if test='status != null'>   AND status = #{status} </if>" +
            "<if test='status == null'>   AND status &gt;= 0 </if>" +
            "<if test='auditStatus != null'>   AND audit_status = #{auditStatus} </if>" +
            "<if test='publishStatus != null'>   AND publish_status = #{publishStatus} </if>" +
            "<if test='uploaderId != null'>   AND uploader_id = #{uploaderId} </if>" +
            "<if test='keyword != null and keyword != \"\"'>   AND (title LIKE CONCAT('%', #{keyword}, '%') OR lesson_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<PrimaryChineseResource> findByConditionPage(
            Page<PrimaryChineseResource> page,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("brandCode") String brandCode,
            @Param("subType") String subType,
            @Param("unitName") String unitName,
            @Param("lessonName") String lessonName,
            @Param("fileExt") String fileExt,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("publishStatus") Integer publishStatus,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 管理端分页（默认排除回收站 status=4，支持推荐/置顶/免费筛选与数据范围）
     */
    @Select("<script>" +
            "SELECT * FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='auditStatus != null'> AND audit_status = #{auditStatus} </if>" +
            "<if test='status == null and excludeRecycleBin'> AND status != 4 </if>" +
            "<if test='stage != null and stage != \"\"'> AND stage = #{stage} </if>" +
            "<if test='stages != null and stages.size() &gt; 0'> AND stage IN " +
            "  <foreach collection='stages' item='s' open='(' separator=',' close=')'>#{s}</foreach> </if>" +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "<if test='subjects != null and subjects.size() &gt; 0'> AND subject IN " +
            "  <foreach collection='subjects' item='sub' open='(' separator=',' close=')'>#{sub}</foreach> </if>" +
            "<if test='scopeUploaderId != null'> AND uploader_id = #{scopeUploaderId} </if>" +
            "<if test='module != null and module != \"\"'> AND module = #{module} </if>" +
            "<if test='type != null and type != \"\"'> AND type = #{type} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND edition = #{edition} </if>" +
            "<if test='isRecommend != null'> AND is_recommend = #{isRecommend} </if>" +
            "<if test='isTop != null'> AND is_top = #{isTop} </if>" +
            "<if test='isFree != null'> AND is_free = #{isFree} </if>" +
            "<if test='uploaderId != null'> AND uploader_id = #{uploaderId} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (title LIKE CONCAT('%', #{keyword}, '%') " +
            "  OR lesson_name LIKE CONCAT('%', #{keyword}, '%') OR unit_name LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<PrimaryChineseResource> adminFindPage(
            Page<PrimaryChineseResource> page,
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module,
            @Param("type") String type,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("status") Integer status,
            @Param("auditStatus") Integer auditStatus,
            @Param("isRecommend") Integer isRecommend,
            @Param("isTop") Integer isTop,
            @Param("isFree") Integer isFree,
            @Param("uploaderId") Long uploaderId,
            @Param("keyword") String keyword,
            @Param("excludeRecycleBin") boolean excludeRecycleBin,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("scopeUploaderId") Long scopeUploaderId,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 查询所有年级（去重，用于前端筛选枚举）
     */
    @Select("SELECT DISTINCT grade_name FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 ORDER BY grade_name")
    List<String> findDistinctGradeNames();

    @Select("<script>" +
            "SELECT DISTINCT grade_name FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND stage = #{stage} " +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "ORDER BY grade_name" +
            "</script>")
    List<String> findDistinctGradeNamesByStageAndSubject(
            @Param("stage") String stage,
            @Param("subject") String subject);

    @Select("SELECT DISTINCT edition FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND stage = #{stage} AND subject = #{subject} ORDER BY edition")
    List<String> findDistinctEditionsByStageAndSubject(
            @Param("stage") String stage,
            @Param("subject") String subject);

    @Select("<script>" +
            "SELECT DISTINCT module FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND stage = #{stage} " +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "ORDER BY module" +
            "</script>")
    List<String> findDistinctModulesByStageAndSubject(
            @Param("stage") String stage,
            @Param("subject") String subject);

    @Select("<script>" +
            "SELECT DISTINCT type FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND stage = #{stage} " +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "<if test='module != null and module != \"\"'> AND module = #{module} </if>" +
            "ORDER BY type" +
            "</script>")
    List<String> findDistinctTypesByStageAndSubject(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("module") String module);

    @Select("<script>" +
            "SELECT r.* FROM xinketang.oss_primary_chinese_resource r " +
            "WHERE r.is_deleted = 0 AND r.catalog_node_id IS NOT NULL AND r.catalog_node_id &gt; 0 " +
            "AND r.catalog_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach> " +
            "AND NOT EXISTS (" +
            "  SELECT 1 FROM edu_resource_placement p " +
            "  WHERE p.resource_id = r.id AND p.catalog_node_id = r.catalog_node_id" +
            ")" +
            "</script>")
    List<PrimaryChineseResource> listMissingPlacementByNodeIds(@Param("nodeIds") List<Long> nodeIds);

    /**
     * 查询所有版本（去重，用于前端筛选枚举）
     */
    @Select("SELECT DISTINCT edition FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 ORDER BY edition")
    List<String> findDistinctEditions();

    /**
     * 查询所有栏目（去重）
     */
    @Select("SELECT DISTINCT module FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 ORDER BY module")
    List<String> findDistinctModules();

    /**
     * 查询所有资源类型（去重）
     */
    @Select("SELECT DISTINCT type FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 ORDER BY type")
    List<String> findDistinctTypes();

    /**
     * 查询指定年级+版本下的所有单元（去重，用于侧边树）
     */
    @Select("<script>" +
            "SELECT DISTINCT unit_name FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 " +
            "<if test='gradeName != null and gradeName != \"\"'>   AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'>   AND edition = #{edition} </if>" +
            "ORDER BY unit_name" +
            "</script>")
    List<String> findDistinctUnitNames(
            @Param("gradeName") String gradeName,
            @Param("edition") String edition
    );

    /**
     * 查询单元-课文对（用于合并单元树）
     */
    @Select("<script>" +
            "SELECT DISTINCT unit_name AS unitName, lesson_name AS lessonName " +
            "FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND status &gt;= 0 " +
            "AND unit_name IS NOT NULL AND unit_name != '' " +
            "<if test='gradeName != null and gradeName != \"\"'> AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND edition LIKE CONCAT('%', #{edition}, '%') </if>" +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "ORDER BY unit_name, lesson_name" +
            "</script>")
    List<Map<String, String>> findUnitLessonPairs(
            @Param("gradeName") String gradeName,
            @Param("edition") String edition,
            @Param("subject") String subject
    );

    /**
     * 用户草稿列表
     */
    @Select("SELECT * FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND status = -1 AND uploader_id = #{uploaderId} " +
            "ORDER BY update_time DESC, upload_time DESC")
    List<PrimaryChineseResource> findDraftsByUploader(@Param("uploaderId") Long uploaderId);

    /**
     * 按栏目统计资源数量
     */
    @Select("<script>" +
            "SELECT module AS module_name, COUNT(*) AS resource_count " +
            "FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND (publish_status = 1 OR (publish_status IS NULL AND status = 1)) " +
            "<if test='stage != null and stage != \"\"'> AND stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND grade_name = #{gradeName} </if>" +
            "<if test='edition != null and edition != \"\"'> AND edition LIKE CONCAT('%', #{edition}, '%') </if>" +
            "GROUP BY module ORDER BY resource_count DESC" +
            "</script>")
    List<Map<String, Object>> countByModule(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("gradeName") String gradeName,
            @Param("edition") String edition
    );

    /**
     * 我的上传统计（按上传者）
     */
    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status >= 0")
    long countByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status = 1")
    long countPublishedByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status = 0")
    long countPendingByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status = -1")
    long countDraftByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status = 2")
    long countRejectedByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status = 3")
    long countOfflineByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COALESCE(SUM(view_count),0) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status >= 0")
    long sumViewsByUploader(@Param("uploaderId") Long uploaderId);

    @Select("SELECT COALESCE(SUM(download_count),0) FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND uploader_id = #{uploaderId} AND status >= 0")
    long sumDownloadsByUploader(@Param("uploaderId") Long uploaderId);

    @Select("<script>" +
            "SELECT id, title, status, " +
            "CASE " +
            "  WHEN title = #{title} THEN 'title' " +
            "  WHEN oss_object_key IS NOT NULL AND oss_object_key = #{ossObjectKey} THEN 'oss_key' " +
            "  ELSE 'filename' END AS matchType " +
            "FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND id != #{id} AND status &gt;= 0 " +
            "AND (" +
            "  (title = #{title} AND #{title} IS NOT NULL AND #{title} != '') " +
            "  OR (original_filename = #{originalFilename} AND #{originalFilename} IS NOT NULL AND #{originalFilename} != '') " +
            "  OR (oss_object_key = #{ossObjectKey} AND #{ossObjectKey} IS NOT NULL AND #{ossObjectKey} != '')" +
            ") " +
            "ORDER BY upload_time DESC LIMIT 5" +
            "</script>")
    List<Map<String, Object>> findDuplicateCandidates(
            @Param("id") Long id,
            @Param("title") String title,
            @Param("originalFilename") String originalFilename,
            @Param("ossObjectKey") String ossObjectKey);

    // ==================== Phase 6：分类删除前资源引用检查 ====================

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 AND status >= 0 AND edition = #{editionCode}")
    long countByEdition(@Param("editionCode") String editionCode);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 AND status >= 0 AND grade_name = #{gradeName}")
    long countByGrade(@Param("gradeName") String gradeName);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 AND status >= 0 AND stage = #{stageCode}")
    long countByStage(@Param("stageCode") String stageCode);

    @Select("SELECT COUNT(*) FROM xinketang.oss_primary_chinese_resource WHERE is_deleted = 0 AND status >= 0 AND subject = #{subjectCode}")
    long countBySubject(@Param("subjectCode") String subjectCode);
}
