package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EduSubjectResourceTypeMapper {

    @Select("SELECT resource_type_id FROM edu_subject_resource_type WHERE subject_id = #{subjectId} AND status = 1 ORDER BY sort ASC, resource_type_id ASC")
    List<Integer> selectTypeIdsBySubjectId(@Param("subjectId") Integer subjectId);

    @Select("SELECT COUNT(1) FROM edu_subject_resource_type WHERE subject_id = #{subjectId}")
    Long countBySubjectId(@Param("subjectId") Integer subjectId);

    @Select("<script>" +
            "SELECT rt.id, rt.code, rt.name, rt.icon, rt.sort " +
            "FROM edu_subject_resource_type srt " +
            "JOIN edu_resource_type rt ON srt.resource_type_id = rt.id " +
            "WHERE srt.subject_id = #{subjectId} AND srt.status = 1 AND rt.status = 1 AND rt.parent_id &gt; 0 " +
            "ORDER BY srt.sort ASC, rt.id ASC" +
            "</script>")
    List<Map<String, Object>> selectTypesForSubject(@Param("subjectId") Integer subjectId);

    @Delete("DELETE FROM edu_subject_resource_type WHERE subject_id = #{subjectId}")
    int deleteBySubjectId(@Param("subjectId") Integer subjectId);

    @Insert("INSERT INTO edu_subject_resource_type (subject_id, resource_type_id, sort, status) VALUES (#{subjectId}, #{resourceTypeId}, #{sort}, #{status})")
    int insertLink(
            @Param("subjectId") Integer subjectId,
            @Param("resourceTypeId") Integer resourceTypeId,
            @Param("sort") Integer sort,
            @Param("status") Integer status);
}
