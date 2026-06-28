package com.k12.resource.search;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 教育场景 Query 解析：从 query 提取学段/频道/类型/学科/版本/年级/栏目实体，剩余词作为 must token
 */
@Component
public class SearchQueryParser {

    private final SearchLexiconService lexiconService;
    public SearchQueryParser(SearchLexiconService lexiconService) {
        this.lexiconService = lexiconService;
    }


    private static final Map<String, String> STAGE_ALIASES = Map.ofEntries(
            Map.entry("幼儿", "preschool"),
            Map.entry("学前", "preschool"),
            Map.entry("小学", "primary"),
            Map.entry("初中", "junior"),
            Map.entry("高中", "senior"),
            Map.entry("美术", "art"),
            Map.entry("舞蹈", "dance"),
            Map.entry("preschool", "preschool"),
            Map.entry("primary", "primary"),
            Map.entry("junior", "junior"),
            Map.entry("senior", "senior"),
            Map.entry("art", "art"),
            Map.entry("dance", "dance")
    );

    private static final Map<String, String> CHANNEL_ALIASES = Map.ofEntries(
            Map.entry("主题班会", "class_meeting"),
            Map.entry("班会", "class_meeting"),
            Map.entry("德育", "class_meeting"),
            Map.entry("生涯规划", "career"),
            Map.entry("生涯", "career"),
            Map.entry("升学", "career"),
            Map.entry("传统文化", "culture"),
            Map.entry("国学", "culture"),
            Map.entry("竞赛专区", "competition"),
            Map.entry("竞赛", "competition"),
            Map.entry("奥赛", "competition"),
            Map.entry("专题资源", "topic"),
            Map.entry("专题", "topic"),
            Map.entry("学段资源", "stage_resource")
    );

    private static final Map<String, String> TYPE_ALIASES = Map.ofEntries(
            Map.entry("教案", "教案"),
            Map.entry("教学设计", "教案"),
            Map.entry("课件", "课件"),
            Map.entry("ppt", "课件"),
            Map.entry("PPT", "课件"),
            Map.entry("练习", "练习"),
            Map.entry("学案", "学案"),
            Map.entry("试卷", "试卷"),
            Map.entry("真题", "试卷"),
            Map.entry("模拟卷", "试卷"),
            Map.entry("音频", "音频"),
            Map.entry("图片", "图片"),
            Map.entry("视频", "视频")
    );

    private static final Map<String, String> EDITION_ALIASES = Map.ofEntries(
            Map.entry("人教版", "人教版"),
            Map.entry("人教", "人教版"),
            Map.entry("统编版", "统编版"),
            Map.entry("统编", "统编版"),
            Map.entry("部编版", "统编版"),
            Map.entry("北师大版", "北师大版"),
            Map.entry("北师大", "北师大版"),
            Map.entry("苏教版", "苏教版")
    );

    private static final List<String> SUBJECTS = List.of(
            "语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治", "科学", "美术", "音乐", "体育"
    );

    private static final List<String> GRADE_PATTERNS = List.of(
            "一年级", "二年级", "三年级", "四年级", "五年级", "六年级",
            "七年级", "八年级", "九年级", "高一", "高二", "高三",
            "一年级上册", "一年级下册", "二年级上册", "二年级下册",
            "三年级上册", "三年级下册", "四年级上册", "四年级下册",
            "五年级上册", "五年级下册", "六年级上册", "六年级下册"
    );

    private static final List<String> MODULE_PATTERNS = List.of(
            "同步备课", "期中", "期末", "月考", "一轮复习", "二轮复习", "三轮冲刺",
            "寒暑假", "小升初", "中考", "高考", "真题", "模拟"
    );

    public ParsedSearchQuery parse(String rawQuery) {
        ParsedSearchQuery out = new ParsedSearchQuery();
        if (!StringUtils.hasText(rawQuery)) {
            return out;
        }
        String normalized = rawQuery.trim().replaceAll("\\s+", " ");
        out.setRawQuery(normalized);

        String working = normalized;
        working = extractLongestMatch(working, CHANNEL_ALIASES, (k, v) -> out.setChannelKey(v));
        working = extractLongestMatch(working, STAGE_ALIASES, (k, v) -> out.setStageKey(v));
        working = extractLongestMatch(working, TYPE_ALIASES, (k, v) -> out.setTeachingType(v));
        working = extractLongestMatch(working, EDITION_ALIASES, (k, v) -> out.setEditionName(v));

        for (String module : MODULE_PATTERNS) {
            if (working.contains(module)) {
                out.setModuleName(module);
                working = working.replace(module, " ");
            }
        }
        for (String grade : GRADE_PATTERNS) {
            if (working.contains(grade)) {
                out.setGradeName(grade);
                working = working.replace(grade, " ");
            }
        }
        for (String subject : SUBJECTS) {
            if (working.contains(subject)) {
                out.setSubject(subject);
                working = working.replace(subject, " ");
            }
        }

        List<String> tokens = new ArrayList<>();
        for (String token : working.split("[\\s,，;；]+")) {
            String t = token.trim();
            if (!StringUtils.hasText(t) || lexiconService.isStopWord(t)) {
                continue;
            }
            if (!tokens.contains(t)) {
                tokens.add(t);
            }
        }
        if (tokens.isEmpty() && StringUtils.hasText(normalized)) {
            tokens.add(normalized);
        }
        out.setMustTokens(tokens);
        out.setKeywordText(String.join(" ", tokens));
        return out;
    }

    /**
     * 合并 URL 显式筛选（优先级高于 parser 提取）
     */
    public ParsedSearchQuery mergeFilters(
            ParsedSearchQuery parsed,
            String stage,
            String channel,
            String type,
            String domain) {
        if (StringUtils.hasText(stage)) {
            parsed.setStageKey(stage.trim());
        }
        if (StringUtils.hasText(channel)) {
            parsed.setChannelKey(channel.trim());
        }
        if (StringUtils.hasText(type)) {
            parsed.setTeachingType(type.trim());
        }
        if (StringUtils.hasText(domain)) {
            parsed.setContentDomain(domain.trim());
        }
        return parsed;
    }

    /** @deprecated 使用带 domain 的重载 */
    public ParsedSearchQuery mergeFilters(
            ParsedSearchQuery parsed,
            String stage,
            String channel,
            String type) {
        return mergeFilters(parsed, stage, channel, type, null);
    }

    private interface BiConsumer {
        void accept(String key, String value);
    }

    private String extractLongestMatch(String text, Map<String, String> dict, BiConsumer setter) {
        String bestKey = null;
        for (String key : dict.keySet()) {
            if (text.contains(key) && (bestKey == null || key.length() > bestKey.length())) {
                bestKey = key;
            }
        }
        if (bestKey != null) {
            setter.accept(bestKey, dict.get(bestKey));
            return text.replace(bestKey, " ");
        }
        return text;
    }
}
