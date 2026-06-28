package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.EduResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * edu_resource 资源 Mapper
 * 通过 LEFT JOIN edu_resource_dimension 及各维度表支持多维度筛选查询
 */
@Mapper
public interface EduResourceMapper extends BaseMapper<EduResource> {

    /**
     * 多维度条件分页查询（通过 edu_resource_dimension 关联各维度表）
     * 支持按学段/学科/版本/年级/学期/册别/栏目/类型/单元筛选
     */
    @Select("<script>" +
            "SELECT DISTINCT r.*, " +
            "  s.name AS stage_name, sub.name AS subject_name, e.name AS edition_name, " +
            "  g.name AS grade_name, sem.name AS semester_name, vol.name AS volume_name, " +
            "  m.name AS module_name, rt.name AS resource_type_name, u.name AS unit_name " +
            "FROM xinketang.edu_resource r " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON r.id = rd.resource_id " +
            "LEFT JOIN xinketang.edu_stage s ON rd.stage_id = s.id " +
            "LEFT JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_semester sem ON rd.semester_id = sem.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "LEFT JOIN xinketang.edu_module m ON rd.module_id = m.id " +
            "LEFT JOIN xinketang.edu_resource_type rt ON rd.resource_type_id = rt.id " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "WHERE r.is_deleted = 0 " +
            "<if test='status != null'>   AND r.status = #{status} </if>" +
            "<if test='status == null'>   AND r.status = 1 </if>" +
            "<if test='stageId != null'>   AND rd.stage_id = #{stageId} </if>" +
            "<if test='stageName != null and stageName != \"\"'>   AND s.name = #{stageName} </if>" +
            "<if test='subjectId != null'>   AND rd.subject_id = #{subjectId} </if>" +
            "<if test='subjectName != null and subjectName != \"\"'>   AND sub.name = #{subjectName} </if>" +
            "<if test='editionId != null'>   AND rd.edition_id = #{editionId} </if>" +
            "<if test='editionName != null and editionName != \"\"'>   AND e.name LIKE CONCAT('%', #{editionName}, '%') </if>" +
            "<if test='gradeId != null'>   AND rd.grade_id = #{gradeId} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'>   AND g.name = #{gradeName} </if>" +
            "<if test='semesterId != null'>   AND rd.semester_id = #{semesterId} </if>" +
            "<if test='semesterName != null and semesterName != \"\"'>   AND sem.name = #{semesterName} </if>" +
            "<if test='volumeId != null'>   AND rd.volume_id = #{volumeId} </if>" +
            "<if test='volumeName != null and volumeName != \"\"'>   AND vol.name = #{volumeName} </if>" +
            "<if test='moduleId != null'>   AND rd.module_id = #{moduleId} </if>" +
            "<if test='moduleName != null and moduleName != \"\"'>   AND m.name = #{moduleName} </if>" +
            "<if test='resourceTypeId != null'>   AND rd.resource_type_id = #{resourceTypeId} </if>" +
            "<if test='resourceTypeName != null and resourceTypeName != \"\"'>   AND rt.name = #{resourceTypeName} </if>" +
            "<if test='unitId != null'>   AND rd.unit_id = #{unitId} </if>" +
            "<if test='unitName != null and unitName != \"\"'>   AND u.name LIKE CONCAT('%', #{unitName}, '%') </if>" +
            "<if test='fileExt != null and fileExt != \"\"'>   AND r.file_ext = #{fileExt} </if>" +
            "<if test='keyword != null and keyword != \"\"'>   AND r.title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY ${sortField} ${sortOrder}" +
            "</script>")
    IPage<EduResource> findByDimensionPage(
            Page<EduResource> page,
            @Param("stageId") Integer stageId,
            @Param("stageName") String stageName,
            @Param("subjectId") Integer subjectId,
            @Param("subjectName") String subjectName,
            @Param("editionId") Integer editionId,
            @Param("editionName") String editionName,
            @Param("gradeId") Integer gradeId,
            @Param("gradeName") String gradeName,
            @Param("semesterId") Integer semesterId,
            @Param("semesterName") String semesterName,
            @Param("volumeId") Integer volumeId,
            @Param("volumeName") String volumeName,
            @Param("moduleId") Integer moduleId,
            @Param("moduleName") String moduleName,
            @Param("resourceTypeId") Integer resourceTypeId,
            @Param("resourceTypeName") String resourceTypeName,
            @Param("unitId") Long unitId,
            @Param("unitName") String unitName,
            @Param("fileExt") String fileExt,
            @Param("status") Integer status,
            @Param("keyword") String keyword,
            @Param("sortField") String sortField,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 多维度条件不分页查询（用于成套资源/列表全量）
     */
    @Select("<script>" +
            "SELECT DISTINCT r.*, " +
            "  s.name AS stage_name, sub.name AS subject_name, e.name AS edition_name, " +
            "  g.name AS grade_name, sem.name AS semester_name, vol.name AS volume_name, " +
            "  m.name AS module_name, rt.name AS resource_type_name, u.name AS unit_name " +
            "FROM xinketang.edu_resource r " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON r.id = rd.resource_id " +
            "LEFT JOIN xinketang.edu_stage s ON rd.stage_id = s.id " +
            "LEFT JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_semester sem ON rd.semester_id = sem.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "LEFT JOIN xinketang.edu_module m ON rd.module_id = m.id " +
            "LEFT JOIN xinketang.edu_resource_type rt ON rd.resource_type_id = rt.id " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "WHERE r.is_deleted = 0 " +
            "<if test='status != null'>   AND r.status = #{status} </if>" +
            "<if test='status == null'>   AND r.status = 1 </if>" +
            "<if test='stageId != null'>   AND rd.stage_id = #{stageId} </if>" +
            "<if test='stageName != null and stageName != \"\"'>   AND s.name = #{stageName} </if>" +
            "<if test='subjectId != null'>   AND rd.subject_id = #{subjectId} </if>" +
            "<if test='subjectName != null and subjectName != \"\"'>   AND sub.name = #{subjectName} </if>" +
            "<if test='editionId != null'>   AND rd.edition_id = #{editionId} </if>" +
            "<if test='editionName != null and editionName != \"\"'>   AND e.name LIKE CONCAT('%', #{editionName}, '%') </if>" +
            "<if test='gradeId != null'>   AND rd.grade_id = #{gradeId} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'>   AND g.name = #{gradeName} </if>" +
            "<if test='semesterId != null'>   AND rd.semester_id = #{semesterId} </if>" +
            "<if test='semesterName != null and semesterName != \"\"'>   AND sem.name = #{semesterName} </if>" +
            "<if test='volumeId != null'>   AND rd.volume_id = #{volumeId} </if>" +
            "<if test='volumeName != null and volumeName != \"\"'>   AND vol.name = #{volumeName} </if>" +
            "<if test='moduleId != null'>   AND rd.module_id = #{moduleId} </if>" +
            "<if test='moduleName != null and moduleName != \"\"'>   AND m.name = #{moduleName} </if>" +
            "<if test='resourceTypeId != null'>   AND rd.resource_type_id = #{resourceTypeId} </if>" +
            "<if test='resourceTypeName != null and resourceTypeName != \"\"'>   AND rt.name = #{resourceTypeName} </if>" +
            "<if test='unitId != null'>   AND rd.unit_id = #{unitId} </if>" +
            "<if test='unitName != null and unitName != \"\"'>   AND u.name LIKE CONCAT('%', #{unitName}, '%') </if>" +
            "<if test='fileExt != null and fileExt != \"\"'>   AND r.file_ext = #{fileExt} </if>" +
            "<if test='keyword != null and keyword != \"\"'>   AND r.title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY r.sort DESC, r.upload_time DESC" +
            "</script>")
    List<EduResource> findByDimension(
            @Param("stageId") Integer stageId,
            @Param("stageName") String stageName,
            @Param("subjectId") Integer subjectId,
            @Param("subjectName") String subjectName,
            @Param("editionId") Integer editionId,
            @Param("editionName") String editionName,
            @Param("gradeId") Integer gradeId,
            @Param("gradeName") String gradeName,
            @Param("semesterId") Integer semesterId,
            @Param("semesterName") String semesterName,
            @Param("volumeId") Integer volumeId,
            @Param("volumeName") String volumeName,
            @Param("moduleId") Integer moduleId,
            @Param("moduleName") String moduleName,
            @Param("resourceTypeId") Integer resourceTypeId,
            @Param("resourceTypeName") String resourceTypeName,
            @Param("unitId") Long unitId,
            @Param("unitName") String unitName,
            @Param("fileExt") String fileExt,
            @Param("status") Integer status,
            @Param("keyword") String keyword
    );

    /**
     * 查询所有学段（去重，供前端筛选）
     */
    @Select("SELECT s.id, s.name, s.sort " +
            "FROM xinketang.edu_stage s " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.stage_id = s.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "GROUP BY s.id, s.name, s.sort " +
            "ORDER BY s.sort")
    List<Map<String, Object>> findDistinctStages();

    /**
     * 查询所有学科（去重，按学段过滤）
     */
    @Select("<script>" +
            "SELECT sub.id, sub.name, sub.sort " +
            "FROM xinketang.edu_subject sub " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.subject_id = sub.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "JOIN xinketang.edu_stage s ON rd.stage_id = s.id AND s.name = #{stageName}" +
            "</if>" +
            "GROUP BY sub.id, sub.name, sub.sort " +
            "ORDER BY sub.sort" +
            "</script>")
    List<Map<String, Object>> findDistinctSubjects(@Param("stageName") String stageName);

    /**
     * 查询所有版本（去重）
     */
    @Select("<script>" +
            "SELECT e.id, e.name, e.sort " +
            "FROM xinketang.edu_edition e " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.edition_id = e.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "JOIN xinketang.edu_stage s ON rd.stage_id = s.id AND s.name = #{stageName}" +
            "</if>" +
            "<if test='subjectName != null and subjectName != \"\"'>" +
            "JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id AND sub.name = #{subjectName}" +
            "</if>" +
            "GROUP BY e.id, e.name, e.sort " +
            "ORDER BY e.sort" +
            "</script>")
    List<Map<String, Object>> findDistinctEditions(
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName);

    /**
     * 查询所有年级（去重）
     */
    @Select("<script>" +
            "SELECT g.id, g.name, g.sort " +
            "FROM xinketang.edu_grade g " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.grade_id = g.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "JOIN xinketang.edu_stage s ON rd.stage_id = s.id AND s.name = #{stageName}" +
            "</if>" +
            "GROUP BY g.id, g.name, g.sort " +
            "ORDER BY g.sort" +
            "</script>")
    List<Map<String, Object>> findDistinctGrades(@Param("stageName") String stageName);

    /**
     * 查询所有栏目（去重）
     */
    @Select("<script>" +
            "SELECT m.id, m.name, m.sort " +
            "FROM xinketang.edu_module m " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.module_id = m.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "JOIN xinketang.edu_stage s ON rd.stage_id = s.id AND s.name = #{stageName}" +
            "</if>" +
            "<if test='subjectName != null and subjectName != \"\"'>" +
            "JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id AND sub.name = #{subjectName}" +
            "</if>" +
            "GROUP BY m.id, m.name, m.sort " +
            "ORDER BY m.sort" +
            "</script>")
    List<Map<String, Object>> findDistinctModules(
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName);

    /**
     * 按学段/学科/栏目查询资源库中实际出现的资源类型（仅二级叶子类型）
     */
    @Select("<script>" +
            "SELECT rt.id, rt.name, rt.sort " +
            "FROM xinketang.edu_resource_type rt " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.resource_type_id = rt.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status IN (1, 2) " +
            "WHERE rt.status = 1 AND rt.parent_id &gt; 0 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "AND rd.stage_id IN (SELECT id FROM xinketang.edu_stage WHERE name = #{stageName}) " +
            "</if>" +
            "<if test='subjectName != null and subjectName != \"\"'>" +
            "AND rd.subject_id IN (SELECT id FROM xinketang.edu_subject WHERE name = #{subjectName}) " +
            "</if>" +
            "<if test='moduleName != null and moduleName != \"\"'>" +
            "AND rd.module_id IN (SELECT id FROM xinketang.edu_module WHERE name = #{moduleName}) " +
            "</if>" +
            "GROUP BY rt.id, rt.name, rt.sort " +
            "ORDER BY rt.sort" +
            "</script>")
    List<Map<String, Object>> findDistinctResourceTypes(
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName,
            @Param("moduleName") String moduleName);

    /**
     * 查询指定条件下的单元列表（供侧边树）
     * 支持按年级/版本/册别/学科过滤
     */
    @Select("<script>" +
            "SELECT u.id, u.name, u.sort " +
            "FROM xinketang.edu_unit u " +
            "JOIN xinketang.edu_resource_dimension rd ON rd.unit_id = u.id " +
            "JOIN xinketang.edu_resource r ON rd.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "<if test='gradeName != null and gradeName != \"\"'>" +
            "JOIN xinketang.edu_grade g ON rd.grade_id = g.id AND g.name = #{gradeName}" +
            "</if>" +
            "<if test='editionName != null and editionName != \"\"'>" +
            "JOIN xinketang.edu_edition e ON rd.edition_id = e.id AND e.name LIKE CONCAT('%', #{editionName}, '%')" +
            "</if>" +
            "<if test='volumeName != null and volumeName != \"\"'>" +
            "JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id AND vol.name = #{volumeName}" +
            "</if>" +
            "<if test='subjectName != null and subjectName != \"\"'>" +
            "JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id AND sub.name = #{subjectName}" +
            "</if>" +
            "GROUP BY u.id, u.name, u.sort " +
            "ORDER BY u.sort, u.id" +
            "</script>")
    List<Map<String, Object>> findDistinctUnits(
            @Param("gradeName") String gradeName,
            @Param("editionName") String editionName,
            @Param("volumeName") String volumeName,
            @Param("subjectName") String subjectName);

    /**
     * 统计各栏目下的资源数量（按学段/学科/年级/版本过滤）
     */
    @Select("<script>" +
            "SELECT m.name AS module_name, COUNT(DISTINCT r.id) AS resource_count, m.sort " +
            "FROM xinketang.edu_resource r " +
            "JOIN xinketang.edu_resource_dimension rd ON r.id = rd.resource_id " +
            "JOIN xinketang.edu_module m ON rd.module_id = m.id " +
            "WHERE r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'>" +
            "AND rd.stage_id = (SELECT id FROM xinketang.edu_stage WHERE name = #{stageName} LIMIT 1)" +
            "</if>" +
            "<if test='subjectName != null and subjectName != \"\"'>" +
            "AND rd.subject_id = (SELECT id FROM xinketang.edu_subject WHERE name = #{subjectName} LIMIT 1)" +
            "</if>" +
            "<if test='gradeName != null and gradeName != \"\"'>" +
            "AND rd.grade_id = (SELECT id FROM xinketang.edu_grade WHERE name = #{gradeName} LIMIT 1)" +
            "</if>" +
            "<if test='editionName != null and editionName != \"\"'>" +
            "AND rd.edition_id IN (SELECT id FROM xinketang.edu_edition WHERE name LIKE CONCAT('%', #{editionName}, '%'))" +
            "</if>" +
            "GROUP BY m.name, m.sort ORDER BY m.sort" +
            "</script>")
    List<Map<String, Object>> countByModule(
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName,
            @Param("gradeName") String gradeName,
            @Param("editionName") String editionName);
}
