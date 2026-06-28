package com.k12.common.constant;

/**
 * 收藏等资源关联类型（区分不同业务表的主键，避免 ID 冲突）
 */
public final class ResourceTypeConstants {

    public static final String RESOURCE = "resource";
    public static final String PRIMARY_CHINESE = "primary_chinese";

    private ResourceTypeConstants() {
    }

    public static String normalize(String resourceType) {
        if (resourceType == null || resourceType.isBlank()) {
            return RESOURCE;
        }
        return resourceType.trim();
    }
}
