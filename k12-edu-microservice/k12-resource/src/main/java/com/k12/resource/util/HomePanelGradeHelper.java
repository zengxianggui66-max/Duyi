package com.k12.resource.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 首页试卷专区：前端册别名 ↔ 库内年级/册别
 */
public final class HomePanelGradeHelper {

    private static final Pattern VOLUME_SUFFIX = Pattern.compile("(上册|下册)$");

    private static final Map<String, String> JUNIOR_GRADE_MAP = Map.of(
            "七年级", "初一",
            "八年级", "初二",
            "九年级", "初三"
    );

    private HomePanelGradeHelper() {
    }

    public record ParsedGrade(String fullName, String gradePrefix, String volumeSuffix, String eduGradeName) {
    }

    public static ParsedGrade parse(String fullGradeName, String stageKey) {
        if (fullGradeName == null || fullGradeName.isBlank()) {
            return new ParsedGrade(null, null, null, null);
        }
        String full = fullGradeName.trim();
        String volumeSuffix = null;
        Matcher m = VOLUME_SUFFIX.matcher(full);
        String gradePart = full;
        if (m.find()) {
            volumeSuffix = m.group(1);
            gradePart = full.substring(0, m.start());
        }
        String eduGrade = gradePart;
        if ("junior".equals(stageKey) && JUNIOR_GRADE_MAP.containsKey(gradePart)) {
            eduGrade = JUNIOR_GRADE_MAP.get(gradePart);
        }
        return new ParsedGrade(full, gradePart, volumeSuffix, eduGrade);
    }
}
