package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.HomePanelFeatured;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomePanelFeaturedMapper extends BaseMapper<HomePanelFeatured> {

    @Select("<script>" +
            "SELECT * FROM xinketang.home_panel_featured " +
            "WHERE status = 1 AND panel_code = #{panelCode} AND tab_key = #{tabKey} " +
            "AND (expire_time IS NULL OR expire_time &gt; NOW()) " +
            "<choose>" +
            "  <when test='filterKey != null and filterKey != \"\"'> AND (filter_key = #{filterKey} OR filter_key IS NULL) </when>" +
            "  <otherwise> AND filter_key IS NULL </otherwise>" +
            "</choose>" +
            "<if test='stageKey != null and stageKey != \"\"'> AND (stage_key IS NULL OR stage_key = #{stageKey}) </if>" +
            "<if test='subjectName != null and subjectName != \"\"'> AND (subject_name IS NULL OR subject_name = #{subjectName}) </if>" +
            "<if test='gradeName != null and gradeName != \"\"'> AND (grade_name IS NULL OR grade_name = #{gradeName}) </if>" +
            "ORDER BY sort DESC, id DESC LIMIT #{limit}" +
            "</script>")
    List<HomePanelFeatured> findActive(
            @Param("panelCode") String panelCode,
            @Param("tabKey") String tabKey,
            @Param("filterKey") String filterKey,
            @Param("stageKey") String stageKey,
            @Param("subjectName") String subjectName,
            @Param("gradeName") String gradeName,
            @Param("limit") int limit);
}
