package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.ResourceAudit;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 资源审核 Mapper
 */
@Mapper
public interface ResourceAuditMapper extends BaseMapper<ResourceAudit> {

    @Select("SELECT id, resource_id AS resourceId, status, reason, auditor_id AS auditorId, " +
            "auditor_name AS auditorName, create_time AS createTime " +
            "FROM resource_audit " +
            "WHERE resource_id = #{resourceId} AND status = 2 " +
            "ORDER BY create_time DESC LIMIT 1")
    ResourceAudit selectLatestRejectByResourceId(@Param("resourceId") Long resourceId);
}
