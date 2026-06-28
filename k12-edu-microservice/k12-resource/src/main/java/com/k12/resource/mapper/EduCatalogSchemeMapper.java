package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduCatalogScheme;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface EduCatalogSchemeMapper extends BaseMapper<EduCatalogScheme> {

    @Select("<script>" +
            "SELECT s.* FROM edu_catalog_scheme s " +
            "LEFT JOIN edu_resource_brand b ON s.brand_id = b.id " +
            "WHERE s.status = 1 " +
            "<if test='brandCode != null and brandCode != \"\"'> AND b.code = #{brandCode} </if>" +
            "ORDER BY s.sort ASC, s.id ASC" +
            "</script>")
    List<EduCatalogScheme> listActiveByBrand(@Param("brandCode") String brandCode);

    @Select("SELECT * FROM edu_catalog_scheme WHERE code = #{code} AND status = 1 LIMIT 1")
    EduCatalogScheme findByCode(@Param("code") String code);

    /** 批量查品牌 code（目录方案列表展示用） */
    @Select("<script>"
            + "SELECT id, code FROM edu_resource_brand WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<Map<String, Object>> selectBrandCodesByIds(@Param("ids") Collection<Long> ids);
}
