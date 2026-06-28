package com.k12.common.constant;

/**
 * Resource lifecycle constants.
 *
 * Legacy status is kept for backward compatibility. New code should prefer
 * auditStatus + publishStatus so review and shelf operations can evolve
 * independently.
 */
public final class ResourceStatusConstants {

    public static final int DRAFT = -1;
    public static final int PENDING = 0;
    public static final int PUBLISHED = 1;
    public static final int REJECTED = 2;
    public static final int OFFLINE = 3;
    public static final int DELETED = 4;

    public static final int AUDIT_DRAFT = -1;
    public static final int AUDIT_PENDING = 0;
    public static final int AUDIT_APPROVED = 1;
    public static final int AUDIT_REJECTED = 2;
    public static final int AUDIT_RECHECKING = 3;

    public static final int PUBLISH_UNPUBLISHED = 0;
    public static final int PUBLISH_PUBLISHED = 1;
    public static final int PUBLISH_OFFLINE = 2;
    public static final int PUBLISH_SCHEDULED = 3;
    public static final int PUBLISH_RECYCLED = 4;

    private ResourceStatusConstants() {
    }

    public static int resolveAuditStatus(Integer auditStatus, Integer legacyStatus) {
        if (auditStatus != null) {
            return auditStatus;
        }
        int status = legacyStatus != null ? legacyStatus : PENDING;
        return switch (status) {
            case DRAFT -> AUDIT_DRAFT;
            case PENDING -> AUDIT_PENDING;
            case PUBLISHED, OFFLINE -> AUDIT_APPROVED;
            case REJECTED -> AUDIT_REJECTED;
            case DELETED -> AUDIT_APPROVED;
            default -> AUDIT_PENDING;
        };
    }

    public static int resolvePublishStatus(Integer publishStatus, Integer legacyStatus) {
        if (publishStatus != null) {
            return publishStatus;
        }
        int status = legacyStatus != null ? legacyStatus : PENDING;
        return switch (status) {
            case PUBLISHED -> PUBLISH_PUBLISHED;
            case OFFLINE -> PUBLISH_OFFLINE;
            case DELETED -> PUBLISH_RECYCLED;
            default -> PUBLISH_UNPUBLISHED;
        };
    }

    public static String auditStatusCodeLabel(int auditStatus) {
        return switch (auditStatus) {
            case AUDIT_DRAFT -> "草稿";
            case AUDIT_PENDING -> "待审核";
            case AUDIT_APPROVED -> "审核通过";
            case AUDIT_REJECTED -> "已驳回";
            case AUDIT_RECHECKING -> "复审中";
            default -> "未知";
        };
    }

    public static String publishStatusLabel(int publishStatus) {
        return switch (publishStatus) {
            case PUBLISH_PUBLISHED -> "已上架";
            case PUBLISH_OFFLINE -> "已下架";
            case PUBLISH_SCHEDULED -> "定时上架";
            case PUBLISH_RECYCLED -> "回收站";
            default -> "未上架";
        };
    }

    public static String auditStatusLabel(Integer auditStatus, Integer legacyStatus) {
        return auditStatusCodeLabel(resolveAuditStatus(auditStatus, legacyStatus));
    }

    public static String publishStatusLabel(Integer publishStatus, Integer legacyStatus) {
        return publishStatusLabel(resolvePublishStatus(publishStatus, legacyStatus));
    }

    /** @deprecated use auditStatusLabel(Integer, Integer). */
    @Deprecated(since = "3.7", forRemoval = false)
    public static String auditStatusLabel(int status) {
        return auditStatusCodeLabel(resolveAuditStatus(null, status));
    }

    /** @deprecated use publishStatusLabel(Integer, Integer). */
    @Deprecated(since = "3.7", forRemoval = false)
    public static String shelfStatusLabel(int status) {
        return publishStatusLabel(resolvePublishStatus(null, status));
    }

    /** @deprecated use auditStatus + publishStatus labels separately. */
    @Deprecated(since = "3.7", forRemoval = false)
    public static String statusLabel(int status) {
        return auditStatusLabel(status) + " / " + shelfStatusLabel(status);
    }
}

