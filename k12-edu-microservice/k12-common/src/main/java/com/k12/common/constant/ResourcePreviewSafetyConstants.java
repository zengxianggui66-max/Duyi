package com.k12.common.constant;

/**
 * 资源预览与文件安全状态（oss_primary_chinese_resource.preview_status / file_safety_status）
 */
public final class ResourcePreviewSafetyConstants {

    /** 不可预览 */
    public static final int PREVIEW_NOT_AVAILABLE = 0;
    /** 可预览 */
    public static final int PREVIEW_AVAILABLE = 1;
    /** 待检测 */
    public static final int PREVIEW_PENDING = 2;
    /** 预览失败 */
    public static final int PREVIEW_FAILED = 3;

    /** 未知 */
    public static final int SAFETY_UNKNOWN = 0;
    /** 待检测 */
    public static final int SAFETY_PENDING = 1;
    /** 安全 */
    public static final int SAFETY_SAFE = 2;
    /** 风险 */
    public static final int SAFETY_RISK = 3;

    private ResourcePreviewSafetyConstants() {
    }

    public static int mapFileSafetyString(String status) {
        if (status == null) {
            return SAFETY_UNKNOWN;
        }
        return switch (status) {
            case "pending" -> SAFETY_PENDING;
            case "safe" -> SAFETY_SAFE;
            case "risk" -> SAFETY_RISK;
            default -> SAFETY_UNKNOWN;
        };
    }
}
