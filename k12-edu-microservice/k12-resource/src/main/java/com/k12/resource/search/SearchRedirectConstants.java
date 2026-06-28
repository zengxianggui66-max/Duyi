package com.k12.resource.search;

import java.util.Set;

/**
 * 搜索重定向共享常量（运营配置校验 + 运行时解析）
 */
public final class SearchRedirectConstants {

    public static final Set<String> VAGUE_SUBJECTS = Set.of(
            "语文", "数学", "英语", "物理", "化学", "生物", "历史", "地理", "政治", "科学"
    );

    private SearchRedirectConstants() {
    }

    public static boolean isVagueSubject(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return false;
        }
        return VAGUE_SUBJECTS.contains(keyword.trim());
    }
}
