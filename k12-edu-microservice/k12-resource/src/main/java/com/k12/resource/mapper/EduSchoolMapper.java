package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduSchool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 学校信息 Mapper
 */
@Mapper
public interface EduSchoolMapper extends BaseMapper<EduSchool> {

    /**
     * 通过地区ID查询学校列表
     */
    @Select("SELECT s.*, r.name AS region_name " +
            "FROM xinketang.edu_school s " +
            "LEFT JOIN xinketang.edu_region r ON s.region_id = r.id " +
            "WHERE s.status = 1 AND s.region_id = #{regionId} " +
            "ORDER BY s.sort ASC")
    List<EduSchool> findByRegionId(@Param("regionId") Integer regionId);

    /**
     * 通过地区路径模糊查询学校
     */
    @Select("SELECT s.*, r.name AS region_name " +
            "FROM xinketang.edu_school s " +
            "LEFT JOIN xinketang.edu_region r ON s.region_id = r.id " +
            "WHERE s.status = 1 AND s.region_path LIKE CONCAT('%', #{regionPath}, '%') " +
            "ORDER BY s.sort ASC")
    List<EduSchool> findByRegionPath(@Param("regionPath") String regionPath);

    /**
     * 通过标签查询学校
     */
    @Select("SELECT s.*, r.name AS region_name " +
            "FROM xinketang.edu_school s " +
            "LEFT JOIN xinketang.edu_region r ON s.region_id = r.id " +
            "WHERE s.status = 1 AND s.tags LIKE CONCAT('%', #{tag}, '%') " +
            "ORDER BY s.sort ASC")
    List<EduSchool> findByTag(@Param("tag") String tag);

    /**
     * 查询所有学校（树形结构）
     */
    @Select("SELECT s.id, s.name, s.short_name AS shortName, s.region_path AS regionPath, " +
            "  s.school_type AS schoolType, s.school_level AS schoolLevel, " +
            "  s.tags, r.name AS region_name " +
            "FROM xinketang.edu_school s " +
            "LEFT JOIN xinketang.edu_region r ON s.region_id = r.id " +
            "WHERE s.status = 1 ORDER BY s.region_id ASC, s.sort ASC")
    List<Map<String, Object>> findAllSchools();
}
