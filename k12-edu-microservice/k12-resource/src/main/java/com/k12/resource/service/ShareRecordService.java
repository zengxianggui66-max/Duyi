package com.k12.resource.service;

import com.k12.common.entity.ShareRecord;
import java.util.List;

/**
 * 分享记录 Service 接口
 */
public interface ShareRecordService {

    ShareRecord recordShare(Long resourceId, String resourceType, Long userId, String shareType,
                            String sharePlatform, String ipAddress, String userAgent);

    String buildShareUrl(Long resourceId, String resourceType);

    List<ShareRecord> getResourceShares(Long resourceId, String resourceType);

    List<ShareRecord> getUserShares(Long userId);

    Long getShareCount(Long resourceId, String resourceType);
}
