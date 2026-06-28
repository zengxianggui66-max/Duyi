package com.k12.resource.service.impl;

import com.k12.common.constant.UserResourceActionType;
import com.k12.common.entity.UserResourceAction;
import com.k12.resource.mapper.UserResourceActionMapper;
import com.k12.resource.service.UserResourceActionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@SuppressWarnings("null")
public class UserResourceActionServiceImpl implements UserResourceActionService {

    private final UserResourceActionMapper userResourceActionMapper;
    public UserResourceActionServiceImpl(UserResourceActionMapper userResourceActionMapper) {
        this.userResourceActionMapper = userResourceActionMapper;
    }


    @Override
    public void recordView(Long userId, Long resourceId, String resourceType, String title, String sourceApi) {
        if (userId == null) {
            return;
        }
        UserResourceAction row = baseRow(userId, UserResourceActionType.VIEW, sourceApi);
        row.setResourceId(resourceId);
        row.setResourceType(resourceType);
        row.setTitle(title);
        record(row);
    }

    @Override
    public void recordDownload(Long userId, Long resourceId, String resourceType, String title, String sourceApi) {
        if (userId == null) {
            return;
        }
        UserResourceAction row = baseRow(userId, UserResourceActionType.DOWNLOAD, sourceApi);
        row.setResourceId(resourceId);
        row.setResourceType(resourceType);
        row.setTitle(title);
        record(row);
    }

    @Override
    public void recordSearch(Long userId, String keyword, Integer hitCount, String sourceApi) {
        if (userId == null || !StringUtils.hasText(keyword)) {
            return;
        }
        UserResourceAction row = baseRow(userId, UserResourceActionType.SEARCH, sourceApi);
        row.setKeyword(keyword.trim());
        row.setTitle(keyword.trim());
        row.setHitCount(hitCount);
        record(row);
    }

    @Override
    public void record(UserResourceAction action) {
        if (action == null || action.getUserId() == null || !StringUtils.hasText(action.getActionType())) {
            return;
        }
        userResourceActionMapper.insert(action);
    }

    private UserResourceAction baseRow(Long userId, String actionType, String sourceApi) {
        UserResourceAction row = new UserResourceAction();
        row.setUserId(userId);
        row.setActionType(actionType);
        row.setSourceApi(sourceApi);
        fillRequestMeta(row);
        return row;
    }

    private void fillRequestMeta(UserResourceAction row) {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return;
        }
        HttpServletRequest request = attrs.getRequest();
        row.setIp(request.getRemoteAddr());
        String ua = request.getHeader("User-Agent");
        if (StringUtils.hasText(ua) && ua.length() > 512) {
            ua = ua.substring(0, 512);
        }
        row.setUserAgent(ua);
    }
}
