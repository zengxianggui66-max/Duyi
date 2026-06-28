package com.k12.resource.mapper;

import com.k12.common.dto.AuditSlaVO;
import com.k12.common.dto.AuditWorkloadVO;
import com.k12.common.dto.DailyStatsPointVO;
import com.k12.common.dto.LowAccessResourceVO;
import com.k12.common.dto.RejectStatsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Phase 8：质量治理分析 Mapper
 */
@Mapper
public interface QualityAnalyticsMapper {

    /**
     * 审核 SLA：平均审核时长 + 超时待审
     */
    AuditSlaVO selectAuditSla(
            @Param("since") LocalDate since,
            @Param("overtime24hBound") LocalDate overtime24hBound,
            @Param("overtime48hBound") LocalDate overtime48hBound,
            @Param("overtime72hBound") LocalDate overtime72hBound);

    /**
     * 审核员工作量（按日期范围汇总）
     */
    List<AuditWorkloadVO> selectAuditorWorkload(
            @Param("since") LocalDate since,
            @Param("limit") int limit);

    /**
     * 驳回原因统计
     */
    List<RejectStatsVO> selectRejectStats(@Param("since") LocalDate since);

    /**
     * 资源增长趋势日数据点
     */
    List<DailyStatsPointVO> selectGrowthDailyPoints(
            @Param("since") LocalDate since,
            @Param("sourceType") String sourceType);

    /**
     * 低访问资源分页查询
     */
    List<LowAccessResourceVO> selectLowAccessResources(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("accessLevel") String accessLevel,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 低访问资源总数
     */
    int countLowAccessResources(
            @Param("stage") String stage,
            @Param("subject") String subject,
            @Param("accessLevel") String accessLevel);

    /**
     * 文件安全风险数
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND file_safety_status = 'risk'")
    int countFileSafetyRisk();

    /**
     * 预览失败待处理数
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM preview_fail_queue WHERE status = 0")
    int countPreviewFailPending();

    /**
     * 敏感词总数
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM sys_sensitive_word WHERE status = 1")
    int countSensitiveWords();

    /**
     * 最近N天审核通过/驳回总计（用于计算通过率/驳回率）
     */
    java.util.Map<String, Object> selectApprovalRateSummary(@Param("since") LocalDate since);

    /**
     * 最近N天下架资源总数
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND publish_status = 2 AND update_time >= #{since}")
    int countOfflineSince(@Param("since") LocalDate since);

    /**
     * 当前已上架资源总数
     */
    @org.apache.ibatis.annotations.Select(
            "SELECT COUNT(*) FROM oss_primary_chinese_resource " +
            "WHERE is_deleted = 0 AND publish_status = 1")
    int countPublished();
}
