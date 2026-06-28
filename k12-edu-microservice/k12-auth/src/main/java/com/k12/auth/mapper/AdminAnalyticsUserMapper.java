package com.k12.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminAnalyticsUserMapper {

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0")
    long countTotalUsers();

    @Select("SELECT COUNT(*) FROM user WHERE deleted = 0 AND create_time >= #{since}")
    long countNewUsers(@Param("since") LocalDateTime since);

    @Select("SELECT DATE(create_time) AS day, COUNT(*) AS cnt " +
            "FROM user WHERE deleted = 0 AND create_time >= #{since} " +
            "GROUP BY DATE(create_time) ORDER BY day")
    List<Map<String, Object>> dailyRegistrations(@Param("since") LocalDateTime since);
}
