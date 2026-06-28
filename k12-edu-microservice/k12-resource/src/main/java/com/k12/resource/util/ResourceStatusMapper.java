package com.k12.resource.util;

import com.k12.common.constant.ResourceStatusConstants;

/**
 * 宽表 status ↔ edu_resource status 映射
 */
public final class ResourceStatusMapper {

    /** 宽表：-1草稿 0待审 1已发布 2不通过 3下架 */
    /** 主表：0草稿 1待审 2已发布 3下架 4不通过 */

    private ResourceStatusMapper() {
    }

    public static int wideToEdu(Integer wideStatus) {
        if (wideStatus == null) {
            return 1;
        }
        return switch (wideStatus) {
            case ResourceStatusConstants.DRAFT -> 0;
            case ResourceStatusConstants.PENDING -> 1;
            case ResourceStatusConstants.PUBLISHED -> 2;
            case ResourceStatusConstants.REJECTED -> 4;
            case ResourceStatusConstants.OFFLINE -> 3;
            default -> 1;
        };
    }

    public static int eduToWide(Integer eduStatus) {
        if (eduStatus == null) {
            return ResourceStatusConstants.PENDING;
        }
        return switch (eduStatus) {
            case 0 -> ResourceStatusConstants.DRAFT;
            case 1 -> ResourceStatusConstants.PENDING;
            case 2 -> ResourceStatusConstants.PUBLISHED;
            case 3 -> ResourceStatusConstants.OFFLINE;
            case 4 -> ResourceStatusConstants.REJECTED;
            default -> ResourceStatusConstants.PENDING;
        };
    }
}
