package com.k12.resource.util;

import java.util.Map;

/**
 * 学段中文名 ↔ 前端 stageKey
 */
public final class StageKeyHelper {

    private static final Map<String, String> NAME_TO_KEY = Map.of(
            "幼儿", "preschool",
            "小学", "primary",
            "初中", "junior",
            "高中", "senior",
            "美术", "art",
            "舞蹈", "dance"
    );

    private StageKeyHelper() {
    }

    public static String toStageKey(String stageName) {
        if (stageName == null || stageName.isBlank()) {
            return null;
        }
        return NAME_TO_KEY.getOrDefault(stageName.trim(), null);
    }

    public static String toStageName(String stageKey) {
        if (stageKey == null || stageKey.isBlank()) {
            return null;
        }
        return switch (stageKey.trim()) {
            case "preschool" -> "幼儿";
            case "primary" -> "小学";
            case "junior" -> "初中";
            case "senior" -> "高中";
            case "art" -> "美术";
            case "dance" -> "舞蹈";
            default -> null;
        };
    }
}
