package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 首页专区资源查询（edu_resource / oss 宽表 / 成套）
 */
@Mapper
public interface HomePanelResourceMapper {

    @Select("<script>" +
            "SELECT DISTINCT r.id, r.title, r.upload_time AS upload_time " +
            "FROM xinketang.edu_resource r " +
            "JOIN xinketang.edu_resource_dimension rd ON r.id = rd.resource_id " +
            "LEFT JOIN xinketang.edu_stage s ON rd.stage_id = s.id " +
            "LEFT JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "LEFT JOIN xinketang.edu_module m ON rd.module_id = m.id " +
            "LEFT JOIN xinketang.edu_resource_type rt ON rd.resource_type_id = rt.id " +
            "LEFT JOIN xinketang.edu_exam_scene ex ON rd.exam_scene_id = ex.id " +
            "WHERE r.is_deleted = 0 AND r.status = 1 " +
            "<if test='stageName != null and stageName != \"\"'> AND s.name = #{stageName} </if>" +
            "<if test='subjectName != null and subjectName != \"\"'> AND sub.name = #{subjectName} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND (" +
            "  g.name = #{gradeName} OR vol.name = #{gradeName} " +
            "  OR CONCAT(IFNULL(g.name,''), IFNULL(vol.name,'')) = #{gradeName} " +
            "  OR CONCAT(IFNULL(g.name,''), IFNULL(vol.name,'')) LIKE CONCAT(#{gradePrefix}, '%') " +
            ") </if>" +
            "<if test='gradePrefix != null and gradePrefix != \"\" and volumeSuffix != null and volumeSuffix != \"\"'> AND (" +
            "  (g.name = #{gradePrefix} AND vol.name = #{volumeSuffix}) " +
            "  OR CONCAT(IFNULL(g.name,''), IFNULL(vol.name,'')) = #{gradeName} " +
            ") </if>" +
            "<choose>" +
            "  <when test='examLevels != null and examLevels.size() &gt; 0'> AND (" +
            "    ex.exam_level IN <foreach collection='examLevels' item='el' open='(' separator=',' close=')'>#{el}</foreach> " +
            "    <if test='moduleNames != null and moduleNames.size() &gt; 0'> OR m.name IN " +
            "      <foreach collection='moduleNames' item='mn' open='(' separator=',' close=')'>#{mn}</foreach> " +
            "      OR EXISTS (SELECT 1 FROM xinketang.edu_resource_module rm " +
            "        JOIN xinketang.edu_module mm ON rm.module_id = mm.id " +
            "        WHERE rm.resource_id = r.id AND mm.name IN " +
            "        <foreach collection='moduleNames' item='mn2' open='(' separator=',' close=')'>#{mn2}</foreach>)" +
            "    </if>" +
            "  ) </when>" +
            "  <when test='moduleNames != null and moduleNames.size() &gt; 0'> AND (" +
            "    m.name IN <foreach collection='moduleNames' item='mn3' open='(' separator=',' close=')'>#{mn3}</foreach> " +
            "    OR EXISTS (SELECT 1 FROM xinketang.edu_resource_module rm " +
            "      JOIN xinketang.edu_module mm ON rm.module_id = mm.id " +
            "      WHERE rm.resource_id = r.id AND mm.name IN " +
            "      <foreach collection='moduleNames' item='mn4' open='(' separator=',' close=')'>#{mn4}</foreach>)" +
            "  ) </when>" +
            "</choose>" +
            "<if test='excludeModuleNames != null and excludeModuleNames.size() &gt; 0'> AND NOT EXISTS (" +
            "  SELECT 1 FROM xinketang.edu_resource_dimension rd2 " +
            "  JOIN xinketang.edu_module mx ON rd2.module_id = mx.id " +
            "  WHERE rd2.resource_id = r.id AND mx.name IN " +
            "  <foreach collection='excludeModuleNames' item='em' open='(' separator=',' close=')'>#{em}</foreach>)" +
            " AND NOT EXISTS (SELECT 1 FROM xinketang.edu_resource_module rm2 " +
            "  JOIN xinketang.edu_module mx2 ON rm2.module_id = mx2.id " +
            "  WHERE rm2.resource_id = r.id AND mx2.name IN " +
            "  <foreach collection='excludeModuleNames' item='em2' open='(' separator=',' close=')'>#{em2}</foreach>)" +
            "</if>" +
            "<if test='resourceTypeNames != null and resourceTypeNames.size() &gt; 0'> AND rt.name IN " +
            "  <foreach collection='resourceTypeNames' item='tn' open='(' separator=',' close=')'>#{tn}</foreach> " +
            "</if>" +
            "<if test='titleKeyword != null and titleKeyword != \"\"'> AND r.title LIKE CONCAT('%', #{titleKeyword}, '%') </if>" +
            "ORDER BY r.upload_time DESC, r.id DESC LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findEduResources(
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName,
            @Param("gradeName") String gradeName,
            @Param("gradePrefix") String gradePrefix,
            @Param("volumeSuffix") String volumeSuffix,
            @Param("moduleNames") List<String> moduleNames,
            @Param("excludeModuleNames") List<String> excludeModuleNames,
            @Param("resourceTypeNames") List<String> resourceTypeNames,
            @Param("titleKeyword") String titleKeyword,
            @Param("examLevels") List<String> examLevels,
            @Param("limit") int limit);

    @Select("<script>" +
            "SELECT id, title, type, upload_time AS upload_time " +
            "FROM xinketang.oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND status = 1 AND audit_status = 1 AND publish_status = 1 " +
            "<if test='stage != null and stage != \"\"'> AND stage = #{stage} </if>" +
            "<if test='subject != null and subject != \"\"'> AND subject = #{subject} </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND (" +
            "  grade_name = #{gradeName} " +
            "  <if test='gradePrefix != null and gradePrefix != \"\"'> OR grade_name LIKE CONCAT(#{gradePrefix}, '%') </if>" +
            ") </if>" +
            "<if test='moduleNames != null and moduleNames.size() &gt; 0'> AND module IN " +
            "  <foreach collection='moduleNames' item='mn' open='(' separator=',' close=')'>#{mn}</foreach> " +
            "</if>" +
            "<if test='excludeModuleNames != null and excludeModuleNames.size() &gt; 0'> AND module NOT IN " +
            "  <foreach collection='excludeModuleNames' item='em' open='(' separator=',' close=')'>#{em}</foreach> " +
            "</if>" +
            "<if test='resourceTypeNames != null and resourceTypeNames.size() &gt; 0'> AND type IN " +
            "  <foreach collection='resourceTypeNames' item='tn' open='(' separator=',' close=')'>#{tn}</foreach> " +
            "</if>" +
            "<if test='titleKeyword != null and titleKeyword != \"\"'> AND title LIKE CONCAT('%', #{titleKeyword}, '%') </if>" +
            "ORDER BY upload_time DESC, id DESC LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findOssResources(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("gradeName") String gradeName,
            @Param("gradePrefix") String gradePrefix,
            @Param("moduleNames") List<String> moduleNames,
            @Param("excludeModuleNames") List<String> excludeModuleNames,
            @Param("resourceTypeNames") List<String> resourceTypeNames,
            @Param("titleKeyword") String titleKeyword,
            @Param("limit") int limit);

    @Select("<script>" +
            "SELECT DISTINCT s.id, s.title, s.update_time AS upload_time " +
            "FROM xinketang.edu_resource_suite s " +
            "LEFT JOIN xinketang.edu_module m ON s.module_id = m.id " +
            "LEFT JOIN xinketang.edu_resource_suite_item si ON si.suite_id = s.id " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON rd.resource_id = si.resource_id " +
            "LEFT JOIN xinketang.edu_stage st ON rd.stage_id = st.id " +
            "LEFT JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id " +
            "WHERE s.status = 1 " +
            "<if test='moduleNames != null and moduleNames.size() &gt; 0'> AND m.name IN " +
            "  <foreach collection='moduleNames' item='mn' open='(' separator=',' close=')'>#{mn}</foreach> " +
            "</if>" +
            "<if test='stageName != null and stageName != \"\"'> AND (st.name = #{stageName} OR st.name IS NULL) </if>" +
            "<if test='subjectName != null and subjectName != \"\"'> AND (sub.name = #{subjectName} OR sub.name IS NULL) </if>" +
            "ORDER BY s.update_time DESC, s.id DESC LIMIT #{limit}" +
            "</script>")
    List<Map<String, Object>> findSuites(
            @Param("moduleNames") List<String> moduleNames,
            @Param("stageName") String stageName,
            @Param("subjectName") String subjectName,
            @Param("limit") int limit);

    @Select("SELECT id, title, upload_time AS upload_time FROM xinketang.edu_resource " +
            "WHERE id = #{id} AND is_deleted = 0 AND status = 1 LIMIT 1")
    Map<String, Object> findEduById(@Param("id") Long id);

    @Select("SELECT id, title, upload_time AS upload_time FROM xinketang.oss_primary_chinese_resource " +
            "WHERE id = #{id} AND is_deleted = 0 AND status = 1 AND audit_status = 1 AND publish_status = 1 LIMIT 1")
    Map<String, Object> findOssById(@Param("id") Long id);

    @Select("SELECT id, title, update_time AS upload_time FROM xinketang.edu_resource_suite " +
            "WHERE id = #{id} AND status = 1 LIMIT 1")
    Map<String, Object> findSuiteById(@Param("id") Long id);
}
