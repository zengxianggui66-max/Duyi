package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduResourceDimension;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EduResourceDimensionMapper extends BaseMapper<EduResourceDimension> {

    @Delete("DELETE FROM edu_resource_dimension WHERE resource_id = #{resourceId}")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
