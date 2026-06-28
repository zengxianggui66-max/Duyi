package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduCatalogNode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EduCatalogNodeMapper extends BaseMapper<EduCatalogNode> {

    @Select("SELECT * FROM edu_catalog_node WHERE scheme_id = #{schemeId} AND status = 1 ORDER BY parent_id ASC, sort ASC, id ASC")
    List<EduCatalogNode> listBySchemeId(@Param("schemeId") Integer schemeId);

    @Select("<script>" +
            "SELECT * FROM edu_catalog_node WHERE scheme_id = #{schemeId} " +
            "<if test='!includeDisabled'> AND status = 1 </if>" +
            "ORDER BY parent_id ASC, sort ASC, id ASC" +
            "</script>")
    List<EduCatalogNode> listAllBySchemeId(
            @Param("schemeId") Integer schemeId,
            @Param("includeDisabled") boolean includeDisabled);

    @Select("SELECT * FROM edu_catalog_node WHERE id = #{id} LIMIT 1")
    EduCatalogNode findById(@Param("id") Long id);

    @Select("SELECT COUNT(*) FROM edu_catalog_node WHERE parent_id = #{parentId} AND status = 1")
    int countActiveChildren(@Param("parentId") Long parentId);

    @Select("SELECT COUNT(*) FROM edu_catalog_node WHERE parent_id = #{parentId}")
    int countChildren(@Param("parentId") Long parentId);

    @Select("SELECT * FROM edu_catalog_node WHERE id = #{id} AND status = 1 LIMIT 1")
    EduCatalogNode findActiveById(@Param("id") Long id);

    @Select("<script>"
            + "SELECT COUNT(DISTINCT p.resource_id) FROM edu_resource_placement p "
            + "INNER JOIN xinketang.oss_primary_chinese_resource r ON r.id = p.resource_id AND r.is_deleted = 0 "
            + "WHERE p.catalog_node_id IN "
            + "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    long countDistinctResourcesByNodeIds(@Param("nodeIds") List<Long> nodeIds);
}
