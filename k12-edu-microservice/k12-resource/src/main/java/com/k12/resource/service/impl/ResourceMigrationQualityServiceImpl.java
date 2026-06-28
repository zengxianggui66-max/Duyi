package com.k12.resource.service.impl;

import com.k12.common.BusinessException;
import com.k12.common.dto.ResourceMigrationQualityDashboardVO;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.ResourceMigrationQualityMapper;
import com.k12.resource.mapper.ResourceCatalogAliasMapper;
import com.k12.resource.service.ResourceMigrationQualityService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@SuppressWarnings("null")
public class ResourceMigrationQualityServiceImpl implements ResourceMigrationQualityService {

    private static final String PERM_VIEW = "admin:quality:sensitive_view";

    private final ResourceMigrationQualityMapper qualityMapper;
    private final ResourceCatalogAliasMapper aliasMapper;
    private final AdminPermissionService adminPermissionService;

    public ResourceMigrationQualityServiceImpl(ResourceMigrationQualityMapper qualityMapper,
                                               ResourceCatalogAliasMapper aliasMapper,
                                               AdminPermissionService adminPermissionService) {
        this.qualityMapper = qualityMapper;
        this.aliasMapper = aliasMapper;
        this.adminPermissionService = adminPermissionService;
    }

    @Override
    public ResourceMigrationQualityDashboardVO getDashboard(Long adminUserId) {
        requireView(adminUserId);
        Map<String, Integer> map = new HashMap<>();
        qualityMapper.selectMigrationQualityMetrics().forEach(row -> {
            String key = String.valueOf(row.get("metric_key"));
            Integer value = parseInt(row.get("metric_value"));
            map.put(key, value);
        });
        ResourceMigrationQualityDashboardVO vo = new ResourceMigrationQualityDashboardVO();
        vo.setUnplacedResources(map.getOrDefault("unplaced_resources", 0));
        vo.setEmptyFileResources(map.getOrDefault("empty_file_resources", 0));
        vo.setApprovedNotPublished(map.getOrDefault("approved_not_published", 0));
        vo.setPublishedButInvisible(map.getOrDefault("published_but_invisible", 0));
        vo.setCatalogNodesWithoutResources(map.getOrDefault("catalog_nodes_without_resources", 0));
        vo.setOrphanResourcesWithoutCatalog(map.getOrDefault("orphan_resources_without_catalog", 0));
        vo.setAliasTotal(aliasMapper.selectCount(null).intValue());
        vo.setAliasEnabled(aliasMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.k12.common.entity.ResourceCatalogAlias>()
                        .eq(com.k12.common.entity.ResourceCatalogAlias::getStatus, 1)
        ).intValue());
        return vo;
    }

    private Integer parseInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception ignore) {
            return 0;
        }
    }

    private void requireView(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_VIEW)) {
            throw new BusinessException(403, "无质量看板权限");
        }
    }
}
