package com.k12.resource.mapper;

import com.k12.common.dto.LegacyApiUsageVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface LegacyApiUsageMapper {

    @Insert("""
            INSERT INTO legacy_api_usage_stat(api_path, stat_date, hit_count, last_hit_time, sample_query)
            VALUES(#{apiPath}, CURRENT_DATE(), 1, NOW(), #{sampleQuery})
            ON DUPLICATE KEY UPDATE
              hit_count = hit_count + 1,
              last_hit_time = NOW(),
              sample_query = CASE
                WHEN VALUES(sample_query) IS NULL OR VALUES(sample_query) = '' THEN sample_query
                ELSE VALUES(sample_query)
              END
            """)
    void upsertHit(@Param("apiPath") String apiPath, @Param("sampleQuery") String sampleQuery);

    @Select("""
            SELECT api_path AS apiPath,
                   stat_date AS statDate,
                   hit_count AS hitCount,
                   last_hit_time AS lastHitTime,
                   sample_query AS sampleQuery
            FROM legacy_api_usage_stat
            WHERE stat_date >= DATE_SUB(CURRENT_DATE(), INTERVAL #{days} DAY)
            ORDER BY stat_date DESC, hit_count DESC, api_path ASC
            """)
    List<LegacyApiUsageVO> listRecent(@Param("days") Integer days);
}
