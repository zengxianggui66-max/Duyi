package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.ResourceMain;
import org.apache.ibatis.annotations.*;

/**
 * Phase 7：resource_main 映射表 Mapper（维护用）
 */
@Mapper
public interface ResourceMainMapper extends BaseMapper<ResourceMain> {

    /** 按源表+源ID查全局ID */
    @Select("SELECT id FROM resource_main WHERE source_type = #{sourceType} AND source_id = #{sourceId} LIMIT 1")
    Long findGlobalId(@Param("sourceType") String sourceType, @Param("sourceId") Long sourceId);

    /** 按全局ID查源类型 */
    @Select("SELECT source_type FROM resource_main WHERE id = #{globalId} AND is_deleted = 0 LIMIT 1")
    String findSourceType(@Param("globalId") Long globalId);

    /** 按源表+源ID更新状态 */
    @Update("UPDATE resource_main SET audit_status = #{auditStatus}, publish_status = #{publishStatus}, " +
            "legacy_status = #{legacyStatus} WHERE source_type = #{sourceType} AND source_id = #{sourceId}")
    int syncStatus(@Param("sourceType") String sourceType,
                   @Param("sourceId") Long sourceId,
                   @Param("auditStatus") Integer auditStatus,
                   @Param("publishStatus") Integer publishStatus,
                   @Param("legacyStatus") Integer legacyStatus);
}
