package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.entity.ShareRecord;
import com.k12.resource.mapper.ShareRecordMapper;
import com.k12.resource.service.ShareRecordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 分享记录 Service 实现
 */
@Service
@SuppressWarnings("null")
public class ShareRecordServiceImpl implements ShareRecordService {

    private final ShareRecordMapper shareRecordMapper;
    public ShareRecordServiceImpl(ShareRecordMapper shareRecordMapper) {
        this.shareRecordMapper = shareRecordMapper;
    }


    @Value("${app.frontend-base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    @Override
    public ShareRecord recordShare(Long resourceId, String resourceType, Long userId, String shareType,
                                   String sharePlatform, String ipAddress, String userAgent) {
        String type = ResourceTypeConstants.normalize(resourceType);
        ShareRecord record = new ShareRecord();
        record.setResourceId(resourceId);
        record.setResourceType(type);
        record.setUserId(userId);
        record.setShareType(shareType);
        record.setSharePlatform(sharePlatform);
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.setShareUrl(buildShareUrl(resourceId, type));

        shareRecordMapper.insert(record);
        return record;
    }

    @Override
    public String buildShareUrl(Long resourceId, String resourceType) {
        String base = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
        String type = ResourceTypeConstants.normalize(resourceType);
        if (ResourceTypeConstants.PRIMARY_CHINESE.equals(type)) {
            return base + "/resource/" + resourceId + "?from=subject";
        }
        return base + "/resource/" + resourceId;
    }

    @Override
    public List<ShareRecord> getResourceShares(Long resourceId, String resourceType) {
        String type = ResourceTypeConstants.normalize(resourceType);
        LambdaQueryWrapper<ShareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareRecord::getResourceId, resourceId)
                .eq(ShareRecord::getResourceType, type)
                .orderByDesc(ShareRecord::getCreateTime);
        return shareRecordMapper.selectList(wrapper);
    }

    @Override
    public List<ShareRecord> getUserShares(Long userId) {
        LambdaQueryWrapper<ShareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareRecord::getUserId, userId)
                .orderByDesc(ShareRecord::getCreateTime);
        return shareRecordMapper.selectList(wrapper);
    }

    @Override
    public Long getShareCount(Long resourceId, String resourceType) {
        String type = ResourceTypeConstants.normalize(resourceType);
        LambdaQueryWrapper<ShareRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShareRecord::getResourceId, resourceId)
                .eq(ShareRecord::getResourceType, type);
        return shareRecordMapper.selectCount(wrapper);
    }
}
