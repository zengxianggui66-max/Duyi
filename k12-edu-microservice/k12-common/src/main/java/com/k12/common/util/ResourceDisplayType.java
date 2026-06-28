package com.k12.common.util;

import org.springframework.util.StringUtils;

/**
 * 资源「展示类型」与 DB type/sub_type 的统一映射（与前端类型 Tab 对齐）
 */
public final class ResourceDisplayType {

    private ResourceDisplayType() {
    }

    /**
     * stats 分组 SQL（与 ResourceBrowseMapper 共用，保持角标与 Tab 名一致）
     */
    public static final String SQL_TYPE_GROUP_EXPR =
            "CASE " +
            "  WHEN r.sub_type = '精彩片段' THEN '精彩片段' " +
            "  WHEN r.sub_type = '课文图片' THEN '课文相关图片' " +
            "  WHEN r.type IS NULL OR TRIM(r.type) = '' THEN '其他' " +
            "  ELSE r.type END";

    /**
     * 由实体字段解析展示类型（与 SQL 分组逻辑一致）
     */
    public static String resolveDisplayType(String type, String subType) {
        if ("精彩片段".equals(subType)) {
            return "精彩片段";
        }
        if ("课文图片".equals(subType)) {
            return "课文相关图片";
        }
        if (!StringUtils.hasText(type)) {
            return "其他";
        }
        return type.trim();
    }

    /**
     * 展示类型 Tab → 列表/统计查询用的 type / subType
     */
    public static QueryFilter resolveQueryFilter(String displayType) {
        if (!StringUtils.hasText(displayType) || "全部".equals(displayType)) {
            return QueryFilter.all();
        }
        return switch (displayType) {
            case "精彩片段" -> new QueryFilter("视频", "精彩片段");
            case "课文相关图片" -> new QueryFilter("图片素材", "课文图片");
            default -> new QueryFilter(displayType, null);
        };
    }

    public record QueryFilter(String type, String subType) {
        public static QueryFilter all() {
            return new QueryFilter(null, null);
        }

        public boolean isAll() {
            return type == null && subType == null;
        }
    }
}
