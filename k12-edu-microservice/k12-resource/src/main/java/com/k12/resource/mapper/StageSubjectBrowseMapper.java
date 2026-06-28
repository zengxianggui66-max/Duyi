package com.k12.resource.mapper;

import com.k12.resource.search.StageSubjectRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StageSubjectBrowseMapper {

    @Select("SELECT s.code AS stageKey, s.name AS stageName, sub.code AS subjectKey, sub.name AS subjectName "
            + "FROM edu_stage s "
            + "INNER JOIN edu_subject sub ON sub.stage_id = s.id "
            + "WHERE s.status = 1 AND sub.status = 1 "
            + "ORDER BY s.sort, sub.sort")
    List<StageSubjectRow> selectStageSubjects();
}
