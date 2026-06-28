package com.k12.resource.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * P3 就绪量化报表 SQL
 */
@Mapper
public interface SearchReadinessMapper {

    @Select("""
            SELECT COUNT(*) FROM sys_search_document WHERE status = 1 AND is_deleted = 0
            """)
    long countActiveDocuments();

    @Select("""
            SELECT doc_type AS docType, COUNT(*) AS cnt
            FROM sys_search_document
            WHERE status = 1 AND is_deleted = 0
            GROUP BY doc_type ORDER BY cnt DESC
            """)
    List<Map<String, Object>> docTypeDistribution();

    @Select("""
            SELECT channel_key AS channelKey, COUNT(*) AS cnt
            FROM sys_search_document
            WHERE status = 1 AND is_deleted = 0
            GROUP BY channel_key ORDER BY cnt DESC
            """)
    List<Map<String, Object>> channelDistribution();

    @Select("""
            SELECT COUNT(*) AS totalQueries,
                   ROUND(AVG(cost_ms), 2) AS avgCostMs,
                   MAX(cost_ms) AS maxCostMs
            FROM search_query_log
            WHERE create_time >= #{since} AND blocked_code IS NULL
            """)
    Map<String, Object> queryCostSummary(@Param("since") LocalDateTime since);

    @Select("""
            WITH ranked AS (
              SELECT cost_ms,
                     ROW_NUMBER() OVER (ORDER BY cost_ms) AS rn,
                     COUNT(*) OVER () AS total
              FROM search_query_log
              WHERE create_time >= #{since}
                AND blocked_code IS NULL
                AND cost_ms IS NOT NULL
            )
            SELECT cost_ms AS p95CostMs FROM ranked
            WHERE rn >= CEIL(total * 0.95) ORDER BY rn LIMIT 1
            """)
    Integer p95CostMs(@Param("since") LocalDateTime since);

    @Select("""
            WITH ranked AS (
              SELECT cost_ms,
                     ROW_NUMBER() OVER (ORDER BY cost_ms) AS rn,
                     COUNT(*) OVER () AS total
              FROM search_query_log
              WHERE create_time >= #{since}
                AND blocked_code IS NULL
                AND cost_ms IS NOT NULL
            )
            SELECT cost_ms AS p99CostMs FROM ranked
            WHERE rn >= CEIL(total * 0.99) ORDER BY rn LIMIT 1
            """)
    Integer p99CostMs(@Param("since") LocalDateTime since);

    @Select("""
            SELECT keyword, COUNT(*) AS queryCount,
                   ROUND(AVG(cost_ms), 2) AS avgCostMs,
                   MAX(cost_ms) AS maxCostMs
            FROM search_query_log
            WHERE create_time >= #{since} AND blocked_code IS NULL
            GROUP BY keyword ORDER BY maxCostMs DESC LIMIT #{limit}
            """)
    List<Map<String, Object>> slowestKeywords(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Select("""
            SELECT keyword, normalized_keyword AS normalizedKeyword, COUNT(*) AS zeroCount
            FROM search_query_log
            WHERE create_time >= #{since} AND hit_count = 0 AND blocked_code IS NULL
            GROUP BY keyword, normalized_keyword
            ORDER BY zeroCount DESC LIMIT #{limit}
            """)
    List<Map<String, Object>> topZeroKeywords(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Select("""
            SELECT q.keyword,
                   COUNT(DISTINCT q.id) AS queryCount,
                   COUNT(c.id) AS clickCount,
                   ROUND(COUNT(c.id) / COUNT(DISTINCT q.id), 4) AS ctr
            FROM search_query_log q
            LEFT JOIN search_click_log c
              ON c.keyword = q.keyword
             AND c.create_time >= q.create_time
             AND c.create_time < DATE_ADD(q.create_time, INTERVAL 30 MINUTE)
            WHERE q.create_time >= #{since} AND q.blocked_code IS NULL
            GROUP BY q.keyword
            ORDER BY queryCount DESC LIMIT #{limit}
            """)
    List<Map<String, Object>> ctrByKeyword(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Select("""
            SELECT
              ROUND(SUM(CASE WHEN c.id IS NOT NULL THEN 1 ELSE 0 END) / COUNT(DISTINCT q.id), 4) AS overallCtr
            FROM search_query_log q
            LEFT JOIN search_click_log c
              ON c.keyword = q.keyword
             AND c.create_time >= q.create_time
             AND c.create_time < DATE_ADD(q.create_time, INTERVAL 30 MINUTE)
            WHERE q.create_time >= #{since} AND q.blocked_code IS NULL
            """)
    Double overallCtr(@Param("since") LocalDateTime since);

    @Select("""
            SELECT keyword,
                   ROUND(AVG(position), 2) AS avgClickPosition,
                   SUM(CASE WHEN position <= 3 THEN 1 ELSE 0 END) AS top3Clicks,
                   COUNT(*) AS totalClicks,
                   ROUND(SUM(CASE WHEN position <= 3 THEN 1 ELSE 0 END) / COUNT(*), 4) AS top3ClickRate
            FROM search_click_log
            WHERE create_time >= #{since} AND click_type = 'result'
            GROUP BY keyword ORDER BY totalClicks DESC LIMIT #{limit}
            """)
    List<Map<String, Object>> positionByKeyword(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Select("""
            SELECT
              ROUND(AVG(position), 2) AS avgClickPosition,
              ROUND(SUM(CASE WHEN position <= 3 THEN 1 ELSE 0 END) / COUNT(*), 4) AS top3ClickRate
            FROM search_click_log
            WHERE create_time >= #{since} AND click_type = 'result'
            """)
    Map<String, Object> overallClickPosition(@Param("since") LocalDateTime since);
}
