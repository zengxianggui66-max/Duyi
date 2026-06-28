package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.HomePanelTabConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomePanelTabConfigMapper extends BaseMapper<HomePanelTabConfig> {

    @Select("<script>" +
            "SELECT * FROM xinketang.home_panel_tab_config " +
            "WHERE status = 1 AND panel_code = #{panelCode} AND tab_key = #{tabKey} " +
            "<choose>" +
            "  <when test='filterKey != null and filterKey != \"\"'> AND filter_key = #{filterKey} </when>" +
            "  <otherwise> AND filter_key IS NULL </otherwise>" +
            "</choose>" +
            " LIMIT 1" +
            "</script>")
    HomePanelTabConfig findOne(
            @Param("panelCode") String panelCode,
            @Param("tabKey") String tabKey,
            @Param("filterKey") String filterKey);

    @Select("SELECT filter_key FROM xinketang.home_panel_tab_config " +
            "WHERE status = 1 AND panel_code = 'promotion' AND tab_key = #{examType} " +
            "AND filter_key IS NOT NULL AND filter_key != '' " +
            "ORDER BY sort ASC, id ASC")
    List<String> listPromotionTopicLabels(@Param("examType") String examType);
}
