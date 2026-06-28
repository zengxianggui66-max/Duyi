package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduResourcePlacement;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EduResourcePlacementMapper extends BaseMapper<EduResourcePlacement> {

    @Select("<script>" +
            "SELECT COUNT(DISTINCT p.resource_id) FROM edu_resource_placement p " +
            "INNER JOIN xinketang.oss_primary_chinese_resource r ON r.id = p.resource_id AND r.is_deleted = 0 " +
            "WHERE p.catalog_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    long countDistinctResourcesByNodeIds(@Param("nodeIds") List<Long> nodeIds);

    @Select("<script>" +
            "SELECT COUNT(*) FROM edu_resource_placement WHERE catalog_node_id IN " +
            "<foreach collection='nodeIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>" +
            "</script>")
    long countPlacementsByNodeIds(@Param("nodeIds") List<Long> nodeIds);

    @Delete("DELETE FROM edu_resource_placement WHERE resource_id = #{resourceId}")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
