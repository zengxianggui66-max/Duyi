package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.SinologyReading;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 国学阅读素材 Mapper
 * 支持按多维度查询国学阅读内容
 */
@Mapper
public interface SinologyReadingMapper extends BaseMapper<SinologyReading> {

    /**
     * 通过 unit_id 查询关联的国学阅读素材
     * 关联路径：edu_resource <-> edu_resource_dimension(unit_id, module_id=34) <-> sinology_reading(resource_id)
     */
    @Select("SELECT sr.*, u.name AS unit_name, g.name AS grade_name, " +
            "  e.name AS edition_name, vol.name AS volume_name " +
            "FROM xinketang.sinology_reading sr " +
            "LEFT JOIN xinketang.edu_resource r ON sr.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON rd.resource_id = r.id " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "WHERE sr.status = 1 " +
            "  AND rd.module_id = 34 " +
            "  AND rd.unit_id = #{unitId} " +
            "ORDER BY sr.sort ASC, sr.id ASC")
    List<SinologyReading> findByUnitId(@Param("unitId") Long unitId);

    /**
     * 通过 module_id + unit_id 查询关联资源（通用，可查国学阅读也可查作文）
     */
    @Select("SELECT sr.*, u.name AS unit_name, g.name AS grade_name, " +
            "  e.name AS edition_name, vol.name AS volume_name " +
            "FROM xinketang.sinology_reading sr " +
            "LEFT JOIN xinketang.edu_resource r ON sr.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON rd.resource_id = r.id " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "WHERE sr.status = 1 " +
            "  AND rd.module_id = #{moduleId} " +
            "  AND rd.unit_id = #{unitId} " +
            "ORDER BY sr.sort ASC, sr.id ASC")
    List<SinologyReading> findByUnitIdAndModule(
            @Param("unitId") Long unitId,
            @Param("moduleId") Integer moduleId);

    /**
     * 按多种维度检索国学素材（年级/版本/册别/体裁/朝代）
     */
    @Select("<script>" +
            "SELECT sr.*, u.name AS unit_name, g.name AS grade_name, " +
            "  e.name AS edition_name, vol.name AS volume_name " +
            "FROM xinketang.sinology_reading sr " +
            "LEFT JOIN xinketang.edu_resource r ON sr.resource_id = r.id AND r.is_deleted = 0 AND r.status = 1 " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON rd.resource_id = r.id " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "WHERE sr.status = 1 " +
            "  AND rd.module_id = 34 " +
            "<if test='gradeName != null and gradeName != \"\"'> AND g.name = #{gradeName} </if>" +
            "<if test='editionName != null and editionName != \"\"'> AND e.name LIKE CONCAT('%', #{editionName}, '%') </if>" +
            "<if test='volumeName != null and volumeName != \"\"'> AND vol.name = #{volumeName} </if>" +
            "<if test='genre != null and genre != \"\"'> AND sr.genre = #{genre} </if>" +
            "<if test='dynasty != null and dynasty != \"\"'> AND sr.dynasty = #{dynasty} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND (sr.title LIKE CONCAT('%', #{keyword}, '%') OR sr.content LIKE CONCAT('%', #{keyword}, '%')) </if>" +
            "ORDER BY sr.sort ASC, sr.id ASC" +
            "</script>")
    List<SinologyReading> findByFilters(
            @Param("gradeName") String gradeName,
            @Param("editionName") String editionName,
            @Param("volumeName") String volumeName,
            @Param("genre") String genre,
            @Param("dynasty") String dynasty,
            @Param("keyword") String keyword);

    /**
     * 查询所有可用体裁枚举
     */
    @Select("SELECT DISTINCT genre AS value, genre AS label, COUNT(*) AS cnt " +
            "FROM xinketang.sinology_reading WHERE status = 1 AND genre IS NOT NULL " +
            "GROUP BY genre ORDER BY cnt DESC")
    List<Map<String, Object>> findDistinctGenres();

    /**
     * 查询所有可用朝代枚举
     */
    @Select("SELECT DISTINCT dynasty AS value, dynasty AS label, COUNT(*) AS cnt " +
            "FROM xinketang.sinology_reading WHERE status = 1 AND dynasty IS NOT NULL " +
            "GROUP BY dynasty ORDER BY cnt DESC")
    List<Map<String, Object>> findDistinctDynasties();

    /**
     * 根据ID查询详情（含维度信息）
     */
    @Select("SELECT sr.*, u.name AS unit_name, u.id AS unit_id, " +
            "  g.name AS grade_name, e.name AS edition_name, " +
            "  vol.name AS volume_name, sem.name AS semester_name " +
            "FROM xinketang.sinology_reading sr " +
            "LEFT JOIN xinketang.edu_resource r ON sr.resource_id = r.id AND r.is_deleted = 0 " +
            "LEFT JOIN xinketang.edu_resource_dimension rd ON rd.resource_id = r.id AND rd.module_id = 34 " +
            "LEFT JOIN xinketang.edu_unit u ON rd.unit_id = u.id " +
            "LEFT JOIN xinketang.edu_grade g ON rd.grade_id = g.id " +
            "LEFT JOIN xinketang.edu_edition e ON rd.edition_id = e.id " +
            "LEFT JOIN xinketang.edu_volume vol ON rd.volume_id = vol.id " +
            "LEFT JOIN xinketang.edu_semester sem ON rd.semester_id = sem.id " +
            "WHERE sr.id = #{id}")
    SinologyReading findDetailById(@Param("id") Long id);
}
