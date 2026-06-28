package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.ResourceAuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ResourceAuditLogMapper extends BaseMapper<ResourceAuditLog> {

    @Select("<script>" +
            "SELECT l.id, l.resource_id AS resourceId, l.auditor_id AS auditorId, l.auditor_name AS auditorName, " +
            "l.action, l.before_status AS beforeStatus, l.after_status AS afterStatus, l.reason, l.created_at AS createdAt, " +
            "r.title AS resourceTitle, r.stage, r.subject " +
            "FROM resource_audit_log l " +
            "LEFT JOIN xinketang.oss_primary_chinese_resource r ON l.resource_id = r.id " +
            "WHERE 1=1 " +
            "<if test='resourceId != null'> AND l.resource_id = #{resourceId} </if>" +
            "<if test='auditorId != null'> AND l.auditor_id = #{auditorId} </if>" +
            "<if test='action != null and action != \"\"'> AND l.action = #{action} </if>" +
            "<if test='startTime != null and startTime != \"\"'> AND l.created_at &gt;= #{startTime} </if>" +
            "<if test='endTime != null and endTime != \"\"'> AND l.created_at &lt;= #{endTime} </if>" +
            "<if test='keyword != null and keyword != \"\"'> " +
            "  AND (r.title LIKE CONCAT('%', #{keyword}, '%') OR l.reason LIKE CONCAT('%', #{keyword}, '%')) " +
            "</if>" +
            "ORDER BY l.created_at DESC" +
            "</script>")
    IPage<Map<String, Object>> selectLogPage(
            Page<Map<String, Object>> page,
            @Param("resourceId") Long resourceId,
            @Param("auditorId") Long auditorId,
            @Param("action") String action,
            @Param("keyword") String keyword,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime);

    @Select("SELECT id, resource_id AS resourceId, auditor_id AS auditorId, auditor_name AS auditorName, " +
            "action, before_status AS beforeStatus, after_status AS afterStatus, reason, created_at AS createdAt " +
            "FROM resource_audit_log " +
            "WHERE resource_id = #{resourceId} AND action = 'reject' " +
            "ORDER BY created_at DESC LIMIT 1")
    ResourceAuditLog selectLatestRejectByResourceId(@Param("resourceId") Long resourceId);
}
