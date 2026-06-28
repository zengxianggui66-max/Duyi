package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduEdition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EduEditionMapper extends BaseMapper<EduEdition> {

    @Select("<script>" +
            "SELECT e.id, e.code, e.name, e.short_name, e.publisher, e.year_label, e.sort, e.status, " +
            "e.create_time, e.update_time " +
            "FROM edu_edition e " +
            "INNER JOIN edu_subject_edition se ON se.edition_id = e.id " +
            "WHERE se.subject_id = #{subjectId} " +
            "<if test='!includeDisabled'> AND e.status = 1 </if>" +
            "ORDER BY se.sort ASC, e.sort ASC, e.id ASC" +
            "</script>")
    List<EduEdition> selectBySubjectId(
            @Param("subjectId") Integer subjectId,
            @Param("includeDisabled") boolean includeDisabled);
}
