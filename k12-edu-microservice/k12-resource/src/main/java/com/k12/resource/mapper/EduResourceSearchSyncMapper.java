package com.k12.resource.mapper;

import com.k12.resource.search.EduResourceSearchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EduResourceSearchSyncMapper {

    @Select("SELECT r.id AS resourceId, r.title, r.description, r.file_ext AS fileExt, "
            + "r.download_count AS downloadCount, r.view_count AS viewCount, r.upload_time AS uploadTime, "
            + "s.code AS stageKey, s.name AS stageName, sub.code AS subjectKey, sub.name AS subjectName, "
            + "g.name AS gradeName, e.name AS editionName, m.name AS moduleName, "
            + "rt.name AS resourceTypeName, u.name AS unitName "
            + "FROM xinketang.edu_resource r "
            + "LEFT JOIN xinketang.edu_resource_dimension rd ON r.id = rd.resource_id "
            + "LEFT JOIN xinketang.edu_stage s ON rd.stage_id = s.id "
            + "LEFT JOIN xinketang.edu_subject sub ON rd.subject_id = sub.id "
            + "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id "
            + "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id "
            + "LEFT JOIN xinketang.edu_module m ON rd.module_id = m.id "
            + "LEFT JOIN xinketang.edu_resource_type rt ON rd.resource_type_id = rt.id "
            + "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id "
            + "WHERE r.is_deleted = 0 AND r.status = 1")
    List<EduResourceSearchRow> selectPublishedRows();
}
