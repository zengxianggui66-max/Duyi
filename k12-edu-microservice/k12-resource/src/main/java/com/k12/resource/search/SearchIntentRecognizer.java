package com.k12.resource.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.resource.entity.SysSearchIntentRule;
import com.k12.resource.mapper.SysSearchIntentRuleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * P2 意图识别：归一化 + QueryParser + DB 意图规则
 */
@Service
public class SearchIntentRecognizer {

    private static final Map<String, String> TYPE_KEY_TO_NAME = Map.of(
            "courseware", "课件",
            "lesson_plan", "教案",
            "exam_paper", "试卷"
    );

    private static final Map<String, String> SUBJECT_KEY_TO_NAME = Map.ofEntries(
            Map.entry("chinese", "语文"),
            Map.entry("math", "数学"),
            Map.entry("english", "英语")
    );

    private final SearchChineseNormalizer normalizer;
    private final SearchQueryParser queryParser;
    private final SysSearchIntentRuleMapper intentRuleMapper;
    public SearchIntentRecognizer(SearchChineseNormalizer normalizer, SearchQueryParser queryParser, SysSearchIntentRuleMapper intentRuleMapper) {
        this.normalizer = normalizer;
        this.queryParser = queryParser;
        this.intentRuleMapper = intentRuleMapper;
    }


    private volatile List<SysSearchIntentRule> cachedRules = List.of();

    public ParsedSearchQuery recognize(String rawQuery) {
        if (!StringUtils.hasText(rawQuery)) {
            return new ParsedSearchQuery();
        }
        String normalized = normalizer.normalize(rawQuery);
        ParsedSearchQuery parsed = queryParser.parse(normalized);
        parsed.setRawQuery(rawQuery.trim());
        parsed.setNormalizedQuery(normalized);

        applyDbRules(rawQuery, normalized, parsed);
        applyPrepModuleHint(parsed);
        applyNewsIntentHint(parsed, rawQuery, normalized);
        inferStageFromGrade(parsed);
        syncStructuredKeys(parsed);

        if (parsed.getMustTokens().isEmpty()) {
            parsed.setMustTokens(normalizer.tokenize(rawQuery));
        }
        if (!StringUtils.hasText(parsed.getKeywordText())) {
            parsed.setKeywordText(String.join(" ", parsed.getMustTokens()));
        }
        return parsed;
    }

    public com.k12.common.dto.SearchParsedIntentVO toIntentVo(ParsedSearchQuery parsed) {
        com.k12.common.dto.SearchParsedIntentVO vo = new com.k12.common.dto.SearchParsedIntentVO();
        if (parsed == null) {
            return vo;
        }
        vo.setStage(parsed.getStageKey());
        vo.setSubject(parsed.getSubjectKey() != null ? parsed.getSubjectKey()
                : (parsed.getSubject() != null ? parsed.getSubject() : null));
        vo.setGrade(parsed.getGradeName());
        vo.setResourceType(parsed.getResourceTypeKey() != null ? parsed.getResourceTypeKey()
                : parsed.getTeachingType());
        vo.setModule(parsed.getModuleKey() != null ? parsed.getModuleKey() : parsed.getModuleName());
        vo.setChannel(parsed.getChannelKey());
        vo.setContentDomain(parsed.getContentDomain());
        vo.setNormalizedQuery(parsed.getNormalizedQuery());
        return vo;
    }

    private void applyDbRules(String raw, String normalized, ParsedSearchQuery parsed) {
        String combined = (raw + " " + normalized).toLowerCase(Locale.ROOT);
        for (SysSearchIntentRule rule : loadRules()) {
            if (rule.getStatus() == null || rule.getStatus() != 1) {
                continue;
            }
            String pattern = rule.getPattern();
            if (!StringUtils.hasText(pattern) || !combined.contains(pattern.toLowerCase(Locale.ROOT))) {
                continue;
            }
            applyRule(rule, parsed);
        }
    }

    private void applyRule(SysSearchIntentRule rule, ParsedSearchQuery parsed) {
        String type = rule.getIntentType() == null ? "" : rule.getIntentType().trim().toLowerCase(Locale.ROOT);
        switch (type) {
            case "stage" -> parsed.setStageKey(rule.getTargetKey());
            case "subject" -> {
                parsed.setSubjectKey(rule.getTargetKey());
                if (StringUtils.hasText(rule.getTargetValue())) {
                    parsed.setSubject(rule.getTargetValue());
                } else if (SUBJECT_KEY_TO_NAME.containsKey(rule.getTargetKey())) {
                    parsed.setSubject(SUBJECT_KEY_TO_NAME.get(rule.getTargetKey()));
                }
            }
            case "grade" -> parsed.setGradeName(StringUtils.hasText(rule.getTargetValue())
                    ? rule.getTargetValue() : rule.getTargetKey());
            case "type" -> {
                parsed.setResourceTypeKey(rule.getTargetKey());
                String name = TYPE_KEY_TO_NAME.getOrDefault(rule.getTargetKey(), rule.getTargetValue());
                if (StringUtils.hasText(name)) {
                    parsed.setTeachingType(name);
                }
            }
            case "module" -> {
                parsed.setModuleKey(rule.getTargetKey());
                if (StringUtils.hasText(rule.getTargetValue())) {
                    parsed.setModuleName(rule.getTargetValue());
                }
            }
            case "channel" -> parsed.setChannelKey(rule.getTargetKey());
            case "news" -> parsed.setContentDomain("news");
            default -> {
                // ignore unknown intent types
            }
        }
    }

    /** 备课/教案/课件类词默认关联同步备课模块（仅用 module_name，索引里 module_key 常为空） */
    private void applyPrepModuleHint(ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getModuleName())) {
            return;
        }
        String text = (parsed.getNormalizedQuery() + " " + parsed.getTeachingType()).toLowerCase(Locale.ROOT);
        if (text.contains("课件") || text.contains("教案") || text.contains("备课") || text.contains("教学设计")) {
            parsed.setModuleName("同步备课");
        }
    }

    private void applyNewsIntentHint(ParsedSearchQuery parsed, String raw, String normalized) {
        String combined = (raw + " " + normalized).toLowerCase(Locale.ROOT);
        if (combined.contains("政策") || combined.contains("通知") || combined.contains("资讯")
                || combined.contains("新闻") || combined.contains("教育局") || combined.contains("公告")) {
            parsed.setContentDomain("news");
            if (!StringUtils.hasText(parsed.getChannelKey())) {
                parsed.setChannelKey("news");
            }
        }
    }

    private void inferStageFromGrade(ParsedSearchQuery parsed) {
        if (StringUtils.hasText(parsed.getStageKey()) || !StringUtils.hasText(parsed.getGradeName())) {
            return;
        }
        String grade = parsed.getGradeName();
        if (grade.contains("七年级") || grade.contains("八年级") || grade.contains("九年级")) {
            parsed.setStageKey("junior");
        } else if (grade.contains("高一") || grade.contains("高二") || grade.contains("高三")) {
            parsed.setStageKey("senior");
        } else if (grade.contains("一年级") || grade.contains("二年级") || grade.contains("三年级")
                || grade.contains("四年级") || grade.contains("五年级") || grade.contains("六年级")) {
            parsed.setStageKey("primary");
        }
    }

    private void syncStructuredKeys(ParsedSearchQuery parsed) {
        if (!StringUtils.hasText(parsed.getSubjectKey()) && StringUtils.hasText(parsed.getSubject())) {
            parsed.setSubjectKey(subjectNameToKey(parsed.getSubject()));
        }
        if (!StringUtils.hasText(parsed.getResourceTypeKey()) && StringUtils.hasText(parsed.getTeachingType())) {
            parsed.setResourceTypeKey(typeNameToKey(parsed.getTeachingType()));
        }
    }

    private String subjectNameToKey(String name) {
        return switch (name) {
            case "语文", "中文", "国文" -> "chinese";
            case "数学", "奥数" -> "math";
            case "英语" -> "english";
            default -> name;
        };
    }

    private String typeNameToKey(String name) {
        return switch (name) {
            case "课件" -> "courseware";
            case "教案", "教学设计" -> "lesson_plan";
            case "试卷" -> "exam_paper";
            default -> name;
        };
    }

    private List<SysSearchIntentRule> loadRules() {
        List<SysSearchIntentRule> local = cachedRules;
        if (local != null && !local.isEmpty()) {
            return local;
        }
        synchronized (this) {
            if (cachedRules != null && !cachedRules.isEmpty()) {
                return cachedRules;
            }
            cachedRules = intentRuleMapper.selectList(
                    new LambdaQueryWrapper<SysSearchIntentRule>()
                            .eq(SysSearchIntentRule::getStatus, 1)
                            .orderByDesc(SysSearchIntentRule::getPriority));
            return cachedRules;
        }
    }

    public void refreshCache() {
        cachedRules = intentRuleMapper.selectList(
                new LambdaQueryWrapper<SysSearchIntentRule>()
                        .eq(SysSearchIntentRule::getStatus, 1)
                        .orderByDesc(SysSearchIntentRule::getPriority));
    }
}
