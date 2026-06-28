package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EduSubjectEditionMapper {

    @Select("SELECT edition_id FROM edu_subject_edition WHERE subject_id = #{subjectId} ORDER BY sort ASC, edition_id ASC")
    List<Integer> selectEditionIdsBySubjectId(@Param("subjectId") Integer subjectId);

    @Delete("DELETE FROM edu_subject_edition WHERE subject_id = #{subjectId}")
    int deleteBySubjectId(@Param("subjectId") Integer subjectId);

    @Delete("DELETE FROM edu_subject_edition WHERE edition_id = #{editionId}")
    int deleteByEditionId(@Param("editionId") Integer editionId);

    @Insert("INSERT INTO edu_subject_edition (subject_id, edition_id, sort) VALUES (#{subjectId}, #{editionId}, #{sort})")
    int insertLink(
            @Param("subjectId") Integer subjectId,
            @Param("editionId") Integer editionId,
            @Param("sort") Integer sort);
}
