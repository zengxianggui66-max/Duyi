package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EduModuleSubjectMapper {

    @Select("SELECT module_id FROM edu_module_subject WHERE subject_id = #{subjectId} AND status = 1 ORDER BY sort ASC, module_id ASC")
    List<Integer> selectModuleIdsBySubjectId(@Param("subjectId") Integer subjectId);

    @Select("SELECT COUNT(1) FROM edu_module_subject WHERE subject_id = #{subjectId}")
    Long countBySubjectId(@Param("subjectId") Integer subjectId);

    @Select("<script>" +
            "SELECT m.id, m.code, m.name, m.icon, m.module_category AS moduleCategory, ms.sort " +
            "FROM edu_module_subject ms " +
            "JOIN edu_module m ON ms.module_id = m.id " +
            "JOIN edu_module_stage mst ON m.id = mst.module_id AND mst.stage_id = #{stageId} " +
            "WHERE ms.subject_id = #{subjectId} AND ms.status = 1 AND m.status = 1 " +
            "ORDER BY ms.sort ASC, m.id ASC" +
            "</script>")
    List<Map<String, Object>> selectModulesForSubject(
            @Param("subjectId") Integer subjectId,
            @Param("stageId") Integer stageId);

    @Select("<script>" +
            "SELECT m.id, m.code, m.name, m.icon, m.module_category AS moduleCategory, mst.sort " +
            "FROM edu_module m " +
            "JOIN edu_module_stage mst ON m.id = mst.module_id AND mst.stage_id = #{stageId} " +
            "WHERE m.status = 1 " +
            "ORDER BY mst.sort ASC, m.id ASC" +
            "</script>")
    List<Map<String, Object>> selectStageModules(@Param("stageId") Integer stageId);

    @Delete("DELETE FROM edu_module_subject WHERE subject_id = #{subjectId}")
    int deleteBySubjectId(@Param("subjectId") Integer subjectId);

    @Insert("INSERT INTO edu_module_subject (module_id, subject_id, sort, status) VALUES (#{moduleId}, #{subjectId}, #{sort}, #{status})")
    int insertLink(
            @Param("moduleId") Integer moduleId,
            @Param("subjectId") Integer subjectId,
            @Param("sort") Integer sort,
            @Param("status") Integer status);
}
