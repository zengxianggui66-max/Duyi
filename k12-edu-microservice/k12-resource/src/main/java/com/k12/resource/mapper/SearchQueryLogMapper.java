package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.resource.entity.SearchQueryLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SearchQueryLogMapper extends BaseMapper<SearchQueryLog> {

    @Select("""
            SELECT COUNT(1) FROM search_query_log WHERE create_time >= #{since}
            """)
    long countSince(@Param("since") LocalDateTime since);

    @Select("""
            SELECT COUNT(1) FROM search_query_log
            WHERE create_time >= #{since} AND hit_count = 0 AND blocked_code IS NULL
            """)
    long countZeroSince(@Param("since") LocalDateTime since);

    @Select("""
            SELECT keyword, COUNT(1) AS queryCount,
                   SUM(CASE WHEN hit_count = 0 THEN 1 ELSE 0 END) AS zeroCount
            FROM search_query_log
            WHERE create_time >= #{since} AND blocked_code IS NULL
            GROUP BY keyword
            ORDER BY zeroCount DESC, queryCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> topZeroQueries(@Param("since") LocalDateTime since, @Param("limit") int limit);

    @Select("""
            SELECT keyword, COUNT(1) AS queryCount, AVG(hit_count) AS avgHits
            FROM search_query_log
            WHERE create_time >= #{since} AND blocked_code IS NULL
            GROUP BY keyword
            ORDER BY queryCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> topQueries(@Param("since") LocalDateTime since, @Param("limit") int limit);
}
