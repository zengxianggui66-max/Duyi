package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface EduModuleStageMapper {

    @Select("SELECT stage_id FROM edu_module_stage WHERE module_id = #{moduleId} ORDER BY sort ASC, stage_id ASC")
    List<Integer> selectStageIdsByModuleId(@Param("moduleId") Integer moduleId);

    @Select("SELECT ms.stage_id AS stageId, s.name AS stageName " +
            "FROM edu_module_stage ms " +
            "JOIN edu_stage s ON s.id = ms.stage_id " +
            "WHERE ms.module_id = #{moduleId} " +
            "ORDER BY ms.sort ASC, ms.stage_id ASC")
    List<Map<String, Object>> selectStageRowsByModuleId(@Param("moduleId") Integer moduleId);

    @Delete("DELETE FROM edu_module_stage WHERE module_id = #{moduleId}")
    int deleteByModuleId(@Param("moduleId") Integer moduleId);

    @Insert("INSERT INTO edu_module_stage (module_id, stage_id, sort) VALUES (#{moduleId}, #{stageId}, #{sort})")
    int insertLink(
            @Param("moduleId") Integer moduleId,
            @Param("stageId") Integer stageId,
            @Param("sort") Integer sort);
}
