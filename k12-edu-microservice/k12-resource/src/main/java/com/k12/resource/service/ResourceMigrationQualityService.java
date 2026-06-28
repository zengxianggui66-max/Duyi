package com.k12.resource.service;

import com.k12.common.dto.ResourceMigrationQualityDashboardVO;

public interface ResourceMigrationQualityService {

    ResourceMigrationQualityDashboardVO getDashboard(Long adminUserId);
}
