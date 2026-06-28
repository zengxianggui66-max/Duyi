package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduModule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 栏目 Mapper
 */
@Mapper
public interface EduModuleMapper extends BaseMapper<EduModule> {

    /**
     * 按学段名称查询栏目（edu_module_stage 关联，按学段内 sort 排序）
     */
    @Select("SELECT m.id, m.code, m.name, m.icon, m.module_category, m.description, ms.sort, m.status " +
            "FROM xinketang.edu_module m " +
            "JOIN xinketang.edu_module_stage ms ON m.id = ms.module_id " +
            "JOIN xinketang.edu_stage s ON ms.stage_id = s.id " +
            "WHERE m.status = 1 AND s.name = #{stageName} " +
            "ORDER BY ms.sort ASC, m.id ASC")
    List<Map<String, Object>> findByStageName(@Param("stageName") String stageName);

    /**
     * 按学段 ID 查询栏目
     */
    @Select("<script>" +
            "SELECT m.id, m.code, m.name, m.icon, m.module_category, m.description, ms.sort, m.status " +
            "FROM xinketang.edu_module m " +
            "JOIN xinketang.edu_module_stage ms ON m.id = ms.module_id " +
            "WHERE ms.stage_id = #{stageId} " +
            "<if test='!includeDisabled'> AND m.status = 1 </if>" +
            "ORDER BY ms.sort ASC, m.id ASC" +
            "</script>")
    List<Map<String, Object>> findByStageId(
            @Param("stageId") Integer stageId,
            @Param("includeDisabled") boolean includeDisabled);
}
