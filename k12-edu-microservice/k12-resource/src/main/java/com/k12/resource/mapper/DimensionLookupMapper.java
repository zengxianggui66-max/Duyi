package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DimensionLookupMapper {

    @Select("SELECT id FROM edu_stage WHERE name = #{name} AND status = 1 LIMIT 1")
    Integer findStageIdByName(@Param("name") String name);

    @Select("SELECT id FROM edu_subject WHERE name = #{name} AND status = 1 LIMIT 1")
    Integer findSubjectIdByName(@Param("name") String name);

    @Select("<script>SELECT id FROM edu_subject WHERE name = #{name} " +
            "<if test='stageId != null'> AND stage_id = #{stageId} </if> AND status = 1 LIMIT 1</script>")
    Integer findSubjectIdByNameAndStage(@Param("name") String name, @Param("stageId") Integer stageId);

    @Select("SELECT id FROM edu_edition WHERE name = #{name} OR name LIKE CONCAT('%', #{name}, '%') LIMIT 1")
    Integer findEditionIdByName(@Param("name") String name);

    @Select("<script>SELECT id FROM edu_grade WHERE name = #{name} " +
            "<if test='stageId != null'> AND stage_id = #{stageId} </if> LIMIT 1</script>")
    Integer findGradeIdByName(@Param("name") String name, @Param("stageId") Integer stageId);

    @Select("SELECT id FROM edu_module WHERE name = #{name} AND status = 1 LIMIT 1")
    Integer findModuleIdByName(@Param("name") String name);

    @Select("SELECT id FROM edu_resource_type WHERE name = #{name} AND status = 1 LIMIT 1")
    Integer findResourceTypeIdByName(@Param("name") String name);

    @Select("SELECT e.name FROM edu_edition e " +
            "JOIN edu_subject_edition se ON se.edition_id = e.id " +
            "JOIN edu_subject s ON s.id = se.subject_id " +
            "WHERE s.name = #{subjectName} AND e.status = 1 " +
            "GROUP BY e.id, e.name, e.sort " +
            "ORDER BY MIN(se.sort), e.sort")
    List<String> findEditionNamesBySubject(@Param("subjectName") String subjectName);
}
