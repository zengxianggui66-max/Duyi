package com.k12.resource.util;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 上传位置：按学段规范化的教材册别与年级编码
 */
public final class UploadPlacementCatalog {

    private static final Map<String, List<String>> VOLUMES_BY_STAGE = new LinkedHashMap<>();

    /** K12 学段 code → 允许的年级 code（与 edu_grade.code 对齐） */
    private static final Map<String, Set<String>> GRADE_CODES_BY_STAGE = Map.of(
            "primary", Set.of("grade1", "grade2", "grade3", "grade4", "grade5", "grade6",
                    "grade_1", "grade_2", "grade_3", "grade_4", "grade_5", "grade_6"),
            "junior", Set.of("grade7", "grade8", "grade9",
                    "grade_7", "grade_8", "grade_9"),
            "senior", Set.of("grade10", "grade11", "grade12",
                    "grade_10", "grade_11", "grade_12")
    );

    private static final Map<String, String> STAGE_NAME_TO_CODE = Map.of(
            "小学", "primary",
            "初中", "junior",
            "高中", "senior",
            "幼儿", "preschool",
            "美术", "art",
            "舞蹈", "dance"
    );

    static {
        VOLUMES_BY_STAGE.put("小学", buildGradeVolumes(new String[]{
                "一", "二", "三", "四", "五", "六"
        }, "年级"));
        VOLUMES_BY_STAGE.put("初中", buildGradeVolumes(new String[]{
                "七", "八", "九"
        }, "年级"));
        VOLUMES_BY_STAGE.put("高中", buildSeniorVolumes());
    }

    private UploadPlacementCatalog() {
    }

    public static String resolveStageCode(String stageCodeOrName) {
        if (stageCodeOrName == null || stageCodeOrName.isBlank()) {
            return null;
        }
        String trimmed = stageCodeOrName.trim();
        if (GRADE_CODES_BY_STAGE.containsKey(trimmed) || STAGE_NAME_TO_CODE.containsValue(trimmed)) {
            return trimmed;
        }
        return STAGE_NAME_TO_CODE.get(trimmed);
    }

    public static boolean gradeCodeBelongsToStage(String stageCodeOrName, String gradeCode) {
        if (!org.springframework.util.StringUtils.hasText(stageCodeOrName)
                || !org.springframework.util.StringUtils.hasText(gradeCode)) {
            return false;
        }
        String stageCode = resolveStageCode(stageCodeOrName);
        if (stageCode == null) {
            return true;
        }
        Set<String> allowed = GRADE_CODES_BY_STAGE.get(stageCode);
        if (allowed == null) {
            return true;
        }
        return allowed.contains(gradeCode.trim());
    }

    public static List<String> volumesForStage(String stageName) {
        if (stageName == null || stageName.isBlank()) {
            return List.of();
        }
        return new java.util.ArrayList<>(VOLUMES_BY_STAGE.getOrDefault(stageName.trim(), List.of()));
    }

    public static boolean belongsToStage(String stageName, String gradeName) {
        if (stageName == null || gradeName == null || gradeName.isBlank()) {
            return false;
        }
        String stage = stageName.trim();
        String g = gradeName.trim();
        if ("小学".equals(stage) || "primary".equals(stage)) {
            return (g.contains("一年级") || g.contains("二年级") || g.contains("三年级")
                    || g.contains("四年级") || g.contains("五年级") || g.contains("六年级"))
                    && !g.contains("七年级") && !g.contains("八年级") && !g.contains("九年级");
        }
        if ("初中".equals(stage) || "junior".equals(stage)) {
            return g.contains("七年级") || g.contains("八年级") || g.contains("九年级")
                    || g.contains("初一") || g.contains("初二") || g.contains("初三");
        }
        if ("高中".equals(stage) || "senior".equals(stage)) {
            return g.contains("高一") || g.contains("高二") || g.contains("高三");
        }
        return true;
    }

    private static List<String> buildGradeVolumes(String[] numerals, String suffix) {
        List<String> list = new java.util.ArrayList<>();
        for (String n : numerals) {
            list.add(n + suffix + "上册");
            list.add(n + suffix + "下册");
        }
        return list;
    }

    private static List<String> buildSeniorVolumes() {
        List<String> list = new java.util.ArrayList<>();
        for (String g : new String[]{"高一", "高二", "高三"}) {
            list.add(g + "上册");
            list.add(g + "下册");
        }
        return list;
    }
}
