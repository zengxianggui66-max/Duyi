package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.CollectItemVO;
import com.k12.common.entity.Collection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 收藏 Mapper
 */
@Mapper
public interface CollectionMapper extends BaseMapper<Collection> {

    String STAGE_KEY_CASE = "CASE COALESCE(c.stage, pc.stage) " +
            "WHEN '幼儿' THEN 'preschool' WHEN '小学' THEN 'primary' WHEN '初中' THEN 'junior' WHEN '高中' THEN 'senior' " +
            "WHEN '美术' THEN 'art' WHEN '舞蹈' THEN 'dance' ELSE c.stage_key END";

    @Select("<script>" +
            "SELECT c.id, c.resource_id AS resourceId, c.resource_type AS resourceType, " +
            "COALESCE(c.title, pc.title, er.title) AS title, " +
            "COALESCE(c.stage, pc.stage) AS stage, " +
            "COALESCE(c.stage_key, " + STAGE_KEY_CASE + ") AS stageKey, " +
            "COALESCE(c.subject, pc.subject) AS subject, " +
            "c.subject_key AS subjectKey, " +
            "COALESCE(c.module, pc.module) AS module, " +
            "COALESCE(c.teaching_type, pc.type) AS teachingType, " +
            "COALESCE(c.grade_name, pc.grade_name) AS gradeName, " +
            "COALESCE(c.file_ext, pc.file_ext, er.file_ext) AS fileExt, " +
            "COALESCE(c.oss_url, pc.oss_url, er.oss_url) AS ossUrl, " +
            "c.create_time AS collectTime " +
            "FROM collection c " +
            "LEFT JOIN xinketang.oss_primary_chinese_resource pc " +
            "  ON c.resource_type = 'primary_chinese' AND c.resource_id = pc.id AND pc.is_deleted = 0 " +
            "LEFT JOIN xinketang.edu_resource er " +
            "  ON c.resource_type = 'resource' AND c.resource_id = er.id AND er.is_deleted = 0 " +
            "WHERE c.user_id = #{userId} " +
            "<if test='stageKey != null and stageKey != \"\"'> " +
            "  AND COALESCE(c.stage_key, " + STAGE_KEY_CASE + ") = #{stageKey} " +
            "</if>" +
            "<if test='subject != null and subject != \"\"'> " +
            "  AND (c.subject = #{subject} OR c.subject_key = #{subject} OR pc.subject = #{subject}) " +
            "</if>" +
            "<if test='teachingType != null and teachingType != \"\"'> " +
            "  AND (c.teaching_type = #{teachingType} OR pc.type = #{teachingType}) " +
            "</if>" +
            "ORDER BY c.create_time DESC" +
            "</script>")
    IPage<CollectItemVO> selectCollectPage(
            Page<CollectItemVO> page,
            @Param("userId") Long userId,
            @Param("stageKey") String stageKey,
            @Param("subject") String subject,
            @Param("teachingType") String teachingType);

    @Select("SELECT COUNT(*) FROM collection WHERE user_id = #{userId}")
    long countByUser(@Param("userId") Long userId);

    @Select("SELECT COALESCE(stage_key, 'unknown') AS sk, COUNT(*) AS cnt FROM collection " +
            "WHERE user_id = #{userId} GROUP BY COALESCE(stage_key, 'unknown')")
    List<Map<String, Object>> countGroupByStageKey(@Param("userId") Long userId);
}
