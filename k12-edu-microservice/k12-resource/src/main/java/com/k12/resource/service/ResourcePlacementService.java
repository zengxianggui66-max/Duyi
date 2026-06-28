package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.entity.EduCatalogNode;
import com.k12.common.entity.EduResourcePlacement;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.mapper.EduCatalogNodeMapper;
import com.k12.resource.mapper.EduResourcePlacementMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 资源与目录节点挂载（M3）
 */
@Service
public class ResourcePlacementService {

    private final EduResourcePlacementMapper eduResourcePlacementMapper;
    private final EduCatalogNodeMapper eduCatalogNodeMapper;
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final SearchIndexSyncHook searchIndexSyncHook;
    public ResourcePlacementService(EduResourcePlacementMapper eduResourcePlacementMapper, EduCatalogNodeMapper eduCatalogNodeMapper, PrimaryChineseResourceMapper primaryChineseResourceMapper, SearchIndexSyncHook searchIndexSyncHook) {
        this.eduResourcePlacementMapper = eduResourcePlacementMapper;
        this.eduCatalogNodeMapper = eduCatalogNodeMapper;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    /**
     * 同步主挂载：写入 placement，并回填宽表 catalog_path
     */
    public void syncPrimaryPlacement(PrimaryChineseResource resource) {
        if (resource == null || resource.getId() == null) {
            return;
        }
        try {
            Long nodeId = resource.getCatalogNodeId();
            if (nodeId == null || nodeId <= 0) {
                return;
            }

            EduCatalogNode node = eduCatalogNodeMapper.findActiveById(nodeId);
            if (node != null) {
                if (!StringUtils.hasText(resource.getCatalogPath())) {
                    resource.setCatalogPath(node.getNamePath());
                    primaryChineseResourceMapper.updateById(resource);
                }
            }

            LambdaQueryWrapper<EduResourcePlacement> exists = new LambdaQueryWrapper<>();
            exists.eq(EduResourcePlacement::getResourceId, resource.getId())
                    .eq(EduResourcePlacement::getCatalogNodeId, nodeId);
            if (eduResourcePlacementMapper.selectCount(exists) > 0) {
                return;
            }

            EduResourcePlacement placement = new EduResourcePlacement();
            placement.setResourceId(resource.getId());
            placement.setCatalogNodeId(nodeId);
            placement.setIsPrimary(1);
            placement.setSort(0);
            try {
                eduResourcePlacementMapper.insert(placement);
            } catch (DuplicateKeyException ignored) {
                // uk_res_node 冲突忽略
            }
        } finally {
            searchIndexSyncHook.afterPrimaryChanged(resource.getId());
        }
    }

    /**
     * 宽表已有 catalog_node_id 但缺少 placement 时自动补挂载（兼容 OSS/SQL 批量导入）
     */
    public void ensurePlacementsForNodeIds(List<Long> nodeIds) {
        if (nodeIds == null || nodeIds.isEmpty()) {
            return;
        }
        List<PrimaryChineseResource> missing =
                primaryChineseResourceMapper.listMissingPlacementByNodeIds(nodeIds);
        if (missing.isEmpty()) {
            return;
        }
        for (PrimaryChineseResource row : missing) {
            syncPrimaryPlacement(row);
        }
    }
}
