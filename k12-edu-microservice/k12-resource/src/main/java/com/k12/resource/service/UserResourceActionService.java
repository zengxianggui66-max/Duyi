package com.k12.resource.service;

import com.k12.common.entity.UserResourceAction;

public interface UserResourceActionService {

    void recordView(Long userId, Long resourceId, String resourceType, String title, String sourceApi);

    void recordDownload(Long userId, Long resourceId, String resourceType, String title, String sourceApi);

    void recordSearch(Long userId, String keyword, Integer hitCount, String sourceApi);

    void record(UserResourceAction action);
}
