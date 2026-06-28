package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ResourceMigrationQualityMapper {

    @Select("SELECT metric_key, metric_value FROM v_resource_migration_quality")
    List<Map<String, Object>> selectMigrationQualityMetrics();
}
