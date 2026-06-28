package com.k12.resource.util;

import com.k12.common.entity.EduStage;
import org.springframework.util.StringUtils;

/**
 * Maps edu_module to home overlay sections (sync / review / promotion).
 */
public final class ModuleNavGroupResolver {

    public enum NavGroup {
        SYNC,
        REVIEW,
        PROMOTION
    }

    private ModuleNavGroupResolver() {
    }

    public static NavGroup resolve(String moduleCategory, String moduleCode, String moduleName, EduStage stage) {
        String cat = moduleCategory != null ? moduleCategory.trim() : "";
        String code = moduleCode != null ? moduleCode.trim() : "";
        String name = moduleName != null ? moduleName.trim() : "";
        String stageCode = stage != null && StringUtils.hasText(stage.getCode()) ? stage.getCode().trim() : "";

        if ("sync".equals(cat) || "同步备课".equals(name)) {
            return NavGroup.SYNC;
        }

        if ("transition".equals(cat)) {
            return NavGroup.PROMOTION;
        }

        if (code.startsWith("xsc_") || code.startsWith("zk_") || code.startsWith("gk_")) {
            return NavGroup.PROMOTION;
        }

        if ("review".equals(cat)) {
            return NavGroup.PROMOTION;
        }

        if (matchesStagePromotion(name, stageCode)) {
            return NavGroup.PROMOTION;
        }

        return NavGroup.REVIEW;
    }

    private static boolean matchesStagePromotion(String name, String stageCode) {
        if (!StringUtils.hasText(name) || !StringUtils.hasText(stageCode)) {
            return false;
        }
        return switch (stageCode) {
            case "preschool" -> containsAny(name, "拼音识字", "数学启蒙", "教学启蒙", "习惯养成", "暑假衔接", "幼小衔接");
            case "primary" -> containsAny(name, "小升初", "专题复习", "真题汇编");
            case "junior" -> containsAny(name, "中考", "学业水平", "一轮复习", "二轮", "三轮");
            case "senior" -> containsAny(name, "高考", "学业水平", "一轮复习", "二轮", "三轮");
            case "art", "dance" -> containsAny(name, "专题复习", "考级");
            default -> false;
        };
    }

    private static boolean containsAny(String name, String... keywords) {
        for (String keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String promotionTitle(EduStage stage) {
        if (stage == null || !StringUtils.hasText(stage.getCode())) {
            return "升学备考";
        }
        return switch (stage.getCode().trim()) {
            case "preschool" -> "幼小衔接";
            case "primary" -> "小升初";
            case "junior" -> "中考";
            case "senior" -> "高考";
            default -> "升学备考";
        };
    }
}
