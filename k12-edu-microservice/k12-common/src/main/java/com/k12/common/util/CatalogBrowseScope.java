package com.k12.common.util;

import org.springframework.util.StringUtils;

/**
 * 目录节点 → 浏览统计/列表是否包含子树（前后端口径一致）
 */
public final class CatalogBrowseScope {

    private CatalogBrowseScope() {
    }

    /**
     * @param nodeType   目录节点类型
     * @param depth      树深度（0 通常为课本根）
     * @param code       节点编码（如 xxx_root）
     * @param clientHint 前端传入的 includeSubtree，未知 nodeType 时作兜底
     */
    public static boolean resolveIncludeSubtree(
            String nodeType,
            Integer depth,
            String code,
            Boolean clientHint) {
        if (StringUtils.hasText(nodeType)) {
            switch (nodeType.toLowerCase()) {
                case "lesson":
                case "leaf":
                    return false;
                case "unit":
                case "folder":
                case "section":
                case "textbook":
                case "root":
                    return true;
                default:
                    break;
            }
        }
        if (depth != null && depth == 0) {
            return true;
        }
        if (StringUtils.hasText(code) && code.endsWith("_root")) {
            return true;
        }
        return clientHint == null || Boolean.TRUE.equals(clientHint);
    }
}
