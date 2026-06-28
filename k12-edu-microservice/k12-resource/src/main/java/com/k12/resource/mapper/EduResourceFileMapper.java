package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduResourceFile;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EduResourceFileMapper extends BaseMapper<EduResourceFile> {

    @Delete("DELETE FROM edu_resource_file WHERE resource_id = #{resourceId}")
    int deleteByResourceId(@Param("resourceId") Long resourceId);
}
