package com.k12.resource.service;

import com.k12.common.dto.HomePanelItemVO;
import com.k12.common.dto.HomePanelListVO;
import com.k12.common.entity.HomePanelFeatured;
import com.k12.common.entity.HomePanelTabConfig;
import com.k12.resource.mapper.HomePanelFeaturedMapper;
import com.k12.resource.mapper.HomePanelResourceMapper;
import com.k12.resource.mapper.HomePanelTabConfigMapper;
import com.k12.resource.util.HomePanelGradeHelper;
import com.k12.resource.util.HomePanelJsonHelper;
import com.k12.resource.util.StageKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
public class HomePanelService {

    private static final int DEFAULT_LIMIT = 18;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final HomePanelTabConfigMapper tabConfigMapper;
    private final HomePanelResourceMapper resourceMapper;
    private final HomePanelFeaturedMapper featuredMapper;
    public HomePanelService(HomePanelTabConfigMapper tabConfigMapper, HomePanelResourceMapper resourceMapper, HomePanelFeaturedMapper featuredMapper) {
        this.tabConfigMapper = tabConfigMapper;
        this.resourceMapper = resourceMapper;
        this.featuredMapper = featuredMapper;
    }


    public HomePanelListVO listSyncPrep(String stageKey, String subjectName, String tabKey, Integer limit) {
        HomePanelTabConfig config = resolveConfig("sync_prep", tabKey, null);
        String stageName = StageKeyHelper.toStageName(stageKey);
        return buildList("sync_prep", tabKey, null, stageKey, stageName, subjectName, null,
                config, null, resolveLimit(limit));
    }

    public HomePanelListVO listPaperZone(String stageKey, String gradeName, String tabKey, Integer limit) {
        String filterKey = "entrance".equals(tabKey) ? stageKey : null;
        HomePanelTabConfig config = resolveConfig("paper_zone", tabKey, filterKey);
        String stageName = StageKeyHelper.toStageName(stageKey);
        return buildList("paper_zone", tabKey, filterKey, stageKey, stageName, null, gradeName,
                config, null, resolveLimit(limit));
    }

    public HomePanelListVO listPromotion(String examType, String topic, Integer limit) {
        String topicKey = StringUtils.hasText(topic) ? topic.trim() : "真题";
        HomePanelTabConfig config = resolveConfig("promotion", examType, topicKey);
        String stageName = promotionStageName(examType);
        String stageKey = stageKeyFromExamType(examType);
        List<String> examLevels = promotionExamLevels(examType);
        return buildList("promotion", examType, topicKey, stageKey, stageName, null, null,
                config, examLevels, resolveLimit(limit));
    }

    private HomePanelListVO buildList(
            String panelCode,
            String tabKey,
            String filterKey,
            String stageKey,
            String stageName,
            String subjectName,
            String gradeName,
            HomePanelTabConfig config,
            List<String> examLevels,
            int limit) {

        List<String> moduleNames = HomePanelJsonHelper.parseStringList(config.getModuleNames());
        List<String> excludeModules = HomePanelJsonHelper.parseStringList(config.getExcludeModuleNames());
        List<String> typeNames = HomePanelJsonHelper.parseStringList(config.getResourceTypeNames());
        String titleKeyword = config.getTitleKeyword();

        HomePanelListVO vo = new HomePanelListVO();
        vo.setLimit(limit);

        LinkedHashMap<Long, HomePanelItemVO> merged = new LinkedHashMap<>();

        appendFeatured(merged, panelCode, tabKey, filterKey, stageKey, subjectName, gradeName, limit);

        if ("suite".equalsIgnoreCase(config.getQueryMode())) {
            int remain = limit - merged.size();
            if (remain > 0) {
                for (HomePanelItemVO item : querySuites(moduleNames, stageName, subjectName, remain)) {
                    if (item.getId() != null && !merged.containsKey(item.getId())) {
                        merged.put(item.getId(), item);
                    }
                    if (merged.size() >= limit) {
                        break;
                    }
                }
            }
            vo.setItems(new ArrayList<>(merged.values()));
            trimToLimit(vo, limit);
            return vo;
        }

        HomePanelGradeHelper.ParsedGrade parsed = HomePanelGradeHelper.parse(gradeName, stageKey);
        int fetchEach = limit + 10;

        if (merged.size() < limit && StringUtils.hasText(stageName)) {
            List<Map<String, Object>> eduRows = resourceMapper.findEduResources(
                    stageName, subjectName, gradeName,
                    parsed.gradePrefix(), parsed.volumeSuffix(),
                    moduleNames, excludeModules, typeNames, titleKeyword,
                    examLevels, fetchEach);
            appendRows(merged, eduRows, "edu_resource", limit);
        }

        if (merged.size() < limit && StringUtils.hasText(stageName)) {
            List<Map<String, Object>> ossRows = resourceMapper.findOssResources(
                    stageName, subjectName,
                    parsed.fullName() != null ? parsed.fullName() : gradeName,
                    parsed.gradePrefix(),
                    moduleNames, excludeModules, typeNames, titleKeyword,
                    fetchEach);
            appendRows(merged, ossRows, "oss_primary_chinese", limit);
        }

        vo.setItems(new ArrayList<>(merged.values()));
        trimToLimit(vo, limit);
        return vo;
    }

    private void appendFeatured(
            LinkedHashMap<Long, HomePanelItemVO> merged,
            String panelCode,
            String tabKey,
            String filterKey,
            String stageKey,
            String subjectName,
            String gradeName,
            int limit) {
        List<HomePanelFeatured> featuredList = featuredMapper.findActive(
                panelCode, tabKey, filterKey, stageKey, subjectName, gradeName, limit);
        for (HomePanelFeatured featured : featuredList) {
            if (merged.size() >= limit) {
                return;
            }
            HomePanelItemVO item = loadFeaturedItem(featured);
            if (item != null && item.getId() != null) {
                merged.put(item.getId(), item);
            }
        }
    }

    private HomePanelItemVO loadFeaturedItem(HomePanelFeatured featured) {
        Map<String, Object> row = switch (featured.getResourceSource()) {
            case "edu_resource" -> resourceMapper.findEduById(featured.getResourceId());
            case "oss_primary_chinese" -> resourceMapper.findOssById(featured.getResourceId());
            case "edu_resource_suite" -> resourceMapper.findSuiteById(featured.getResourceId());
            default -> null;
        };
        if (row == null) {
            return null;
        }
        return mapRow(row, featured.getResourceSource());
    }

    private List<HomePanelItemVO> querySuites(
            List<String> moduleNames, String stageName, String subjectName, int limit) {
        List<Map<String, Object>> rows = resourceMapper.findSuites(moduleNames, stageName, subjectName, limit);
        List<HomePanelItemVO> items = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            HomePanelItemVO item = mapRow(row, "edu_resource_suite");
            if (item != null) {
                items.add(item);
            }
        }
        return items;
    }

    private void appendRows(
            LinkedHashMap<Long, HomePanelItemVO> merged,
            List<Map<String, Object>> rows,
            String source,
            int limit) {
        if (rows == null) {
            return;
        }
        for (Map<String, Object> row : rows) {
            if (merged.size() >= limit) {
                return;
            }
            HomePanelItemVO item = mapRow(row, source);
            if (item != null && item.getId() != null && !merged.containsKey(item.getId())) {
                merged.put(item.getId(), item);
            }
        }
    }

    private HomePanelItemVO mapRow(Map<String, Object> row, String source) {
        if (row == null || row.get("id") == null || row.get("title") == null) {
            log.warn("首页专区数据缺失关键字段，source={}, row={}", source, row);
            return null;
        }
        long id = ((Number) row.get("id")).longValue();
        HomePanelItemVO item = new HomePanelItemVO();
        item.setId(id);
        item.setTitle(String.valueOf(row.get("title")));
        item.setDate(formatUploadTime(row.get("upload_time")));
        item.setSource(source);
        item.setDetailPath(detailPath(id));
        return item;
    }

    private String detailPath(long id) {
        return "/resource/" + id;
    }

    private String formatUploadTime(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.format(DATE_FMT);
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().format(DATE_FMT);
        }
        String s = value.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private HomePanelTabConfig resolveConfig(String panelCode, String tabKey, String filterKey) {
        HomePanelTabConfig config = tabConfigMapper.findOne(panelCode, tabKey, filterKey);
        if (config == null && filterKey != null) {
            config = tabConfigMapper.findOne(panelCode, tabKey, null);
        }
        if (config == null) {
            throw new IllegalArgumentException("未找到首页专区配置: " + panelCode + "/" + tabKey
                    + (filterKey != null ? "/" + filterKey : ""));
        }
        return config;
    }

    private void trimToLimit(HomePanelListVO vo, int limit) {
        if (vo.getItems().size() > limit) {
            vo.setItems(vo.getItems().subList(0, limit));
        }
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, 50);
    }

    private String promotionStageName(String examType) {
        return switch (examType) {
            case "kindergarten_bridge" -> "幼儿";
            case "primary_promo" -> "小学";
            case "middle" -> "初中";
            case "high" -> "高中";
            case "vocational_promo" -> "初中";
            default -> null;
        };
    }

    private String stageKeyFromExamType(String examType) {
        return switch (examType) {
            case "kindergarten_bridge" -> "preschool";
            case "primary_promo" -> "primary";
            case "middle" -> "junior";
            case "high" -> "senior";
            case "vocational_promo" -> "junior";
            default -> null;
        };
    }

    private List<String> promotionExamLevels(String examType) {
        return switch (examType) {
            case "kindergarten_bridge" -> List.of();
            case "primary_promo" -> List.of("xsc");
            case "middle" -> List.of("zk", "mock", "real_paper");
            case "high" -> List.of("gk", "mock", "real_paper");
            case "vocational_promo" -> List.of("academic");
            default -> List.of();
        };
    }
}
