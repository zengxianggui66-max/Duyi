package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.resource.entity.SearchClickLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface SearchClickLogMapper extends BaseMapper<SearchClickLog> {

    @Select("""
            SELECT click_type AS clickType, COUNT(1) AS cnt
            FROM search_click_log
            WHERE create_time >= #{since}
            GROUP BY click_type
            """)
    List<Map<String, Object>> countByClickType(@Param("since") LocalDateTime since);

    @Select("""
            SELECT doc_id AS docId, COUNT(1) AS clickCount
            FROM search_click_log
            WHERE keyword = #{keyword} AND doc_id IS NOT NULL AND doc_id != ''
            GROUP BY doc_id
            ORDER BY clickCount DESC
            LIMIT #{limit}
            """)
    List<Map<String, Object>> topDocIdsByKeyword(@Param("keyword") String keyword, @Param("limit") int limit);
}
