package com.k12.resource.mapper;

import com.k12.common.dto.AdminAnalyticsAuditVO;
import com.k12.common.dto.AdminAnalyticsDistributionItemVO;
import com.k12.common.dto.AdminAnalyticsTopResourceVO;
import com.k12.common.dto.AdminAnalyticsUploaderStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminAnalyticsMapper {

    Map<String, Object> selectSummary(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    long countResourcesBefore(
            @Param("before") LocalDateTime before,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<Map<String, Object>> selectDailyUpload(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<Map<String, Object>> selectDailyViews(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<Map<String, Object>> selectDailyDownloads(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<Map<String, Object>> selectDailyCollects(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    AdminAnalyticsAuditVO selectAuditStats(
            @Param("since") LocalDateTime since,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsDistributionItemVO> selectStageDistribution(
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsDistributionItemVO> selectSubjectDistribution(
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsTopResourceVO> selectTopByDownload(
            @Param("limit") int limit,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsTopResourceVO> selectTopByView(
            @Param("limit") int limit,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsTopResourceVO> selectTopByCollect(
            @Param("limit") int limit,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);

    List<AdminAnalyticsUploaderStatVO> selectTopUploaders(
            @Param("since") LocalDateTime since,
            @Param("limit") int limit,
            @Param("stages") List<String> stages,
            @Param("subjects") List<String> subjects,
            @Param("uploaderId") Long uploaderId);
}
