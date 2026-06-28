package com.k12.resource.legacy;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.k12.common.entity.SysConfig;
import com.k12.common.mapper.SysConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Phase 3I-D：旧读 API 410 下线映射（开关默认关，7 天归零后由运维开启）。
 */
@Service
public class LegacyReadApi410Service {

    public static final String FLAG_KEY = "feature.legacyReadApi410.enabled";

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final Set<String> EXACT_GET_PATHS = Set.of(
            "/api/primary-chinese/page",
            "/api/primary-chinese/list",
            "/api/primary-chinese/suites",
            "/api/primary-chinese/module-stats",
            "/api/resources/browse",
            "/api/resources/browse/stats",
            "/api/resources/browse/suites",
            "/api/resources/browse/module-stats",
            "/api/topic/resources/page",
            "/api/culture/resources/page",
            "/api/competition/resources/page"
    );

    private static final Map<String, String> EXACT_REPLACEMENTS = Map.ofEntries(
            Map.entry("/api/primary-chinese/page", "/api/resources/page?sourceType=primary_chinese"),
            Map.entry("/api/primary-chinese/list", "/api/resources/page?sourceType=primary_chinese"),
            Map.entry("/api/primary-chinese/suites", "/api/resources/suites?sourceType=primary_chinese"),
            Map.entry("/api/primary-chinese/module-stats", "/api/resources/module-stats?sourceType=primary_chinese"),
            Map.entry("/api/resources/browse", "/api/resources/page?sourceType=primary_chinese"),
            Map.entry("/api/resources/browse/stats", "/api/resources/stats?sourceType=primary_chinese"),
            Map.entry("/api/resources/browse/suites", "/api/resources/suites?sourceType=primary_chinese"),
            Map.entry("/api/resources/browse/module-stats", "/api/resources/module-stats?sourceType=primary_chinese"),
            Map.entry("/api/topic/resources/page", "/api/resources/page?sourceType=topic_resource"),
            Map.entry("/api/culture/resources/page", "/api/resources/page?sourceType=culture_resource"),
            Map.entry("/api/competition/resources/page", "/api/resources/page?sourceType=competition_resource")
    );

    private static final List<DetailPattern> DETAIL_PATTERNS = List.of(
            new DetailPattern(
                    Pattern.compile("^/api/primary-chinese/(\\d+)$"),
                    "primary_chinese"),
            new DetailPattern(
                    Pattern.compile("^/api/topic/resources/(\\d+)$"),
                    "topic_resource"),
            new DetailPattern(
                    Pattern.compile("^/api/culture/resources/(\\d+)$"),
                    "culture_resource"),
            new DetailPattern(
                    Pattern.compile("^/api/competition/resources/(\\d+)$"),
                    "competition_resource")
    );

    /** 写链路 / 元数据 / 我的资源等 GET 白名单（不返回 410） */
    private static final List<String> READ_EXCLUDE_PATTERNS = List.of(
            "/api/primary-chinese/mine/**",
            "/api/primary-chinese/draft/**",
            "/api/primary-chinese/drafts",
            "/api/primary-chinese/*/file",
            "/api/primary-chinese/filter-options",
            "/api/primary-chinese/upload-filter-options",
            "/api/primary-chinese/grade-names",
            "/api/primary-chinese/editions",
            "/api/primary-chinese/modules",
            "/api/primary-chinese/types",
            "/api/primary-chinese/unit-names",
            "/api/primary-chinese/unit-tree",
            "/api/topic/filter-options",
            "/api/topic/stats",
            "/api/topic/resources/hot",
            "/api/topic/resources/latest",
            "/api/topic/resources/*/view",
            "/api/topic/resources/*/download",
            "/api/culture/filter-options",
            "/api/culture/resources/*/view",
            "/api/culture/resources/*/download",
            "/api/competition/filter-options",
            "/api/competition/resources/*/view",
            "/api/competition/resources/*/download"
    );

    private final SysConfigMapper sysConfigMapper;

    public LegacyReadApi410Service(SysConfigMapper sysConfigMapper) {
        this.sysConfigMapper = sysConfigMapper;
    }

    public boolean is410Enabled() {
        SysConfig row = sysConfigMapper.selectOne(new QueryWrapper<SysConfig>()
                .eq("config_key", FLAG_KEY)
                .last("LIMIT 1"));
        if (row == null || !StringUtils.hasText(row.getConfigValue())) {
            return false;
        }
        return Boolean.parseBoolean(row.getConfigValue().trim());
    }

    public boolean isDeprecatedLegacyRead(String method, String path) {
        if (!"GET".equalsIgnoreCase(method) || !StringUtils.hasText(path)) {
            return false;
        }
        if (isExcluded(path)) {
            return false;
        }
        if (EXACT_GET_PATHS.contains(path)) {
            return true;
        }
        return DETAIL_PATTERNS.stream().anyMatch(pattern -> pattern.matches(path));
    }

    public Optional<String> resolveReplacementLocation(String path) {
        String exact = EXACT_REPLACEMENTS.get(path);
        if (exact != null) {
            return Optional.of(exact);
        }
        for (DetailPattern pattern : DETAIL_PATTERNS) {
            Optional<String> location = pattern.resolveLocation(path);
            if (location.isPresent()) {
                return location;
            }
        }
        return Optional.empty();
    }

    public Map<String, String> listExactReplacements() {
        return new LinkedHashMap<>(EXACT_REPLACEMENTS);
    }

    private boolean isExcluded(String path) {
        for (String pattern : READ_EXCLUDE_PATTERNS) {
            if (PATH_MATCHER.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private record DetailPattern(Pattern pattern, String sourceType) {
        boolean matches(String path) {
            return pattern.matcher(path).matches();
        }

        Optional<String> resolveLocation(String path) {
            Matcher matcher = pattern.matcher(path);
            if (!matcher.matches()) {
                return Optional.empty();
            }
            return Optional.of("/api/resources/resolve-global-id?sourceType="
                    + sourceType + "&sourceId=" + matcher.group(1));
        }
    }
}
