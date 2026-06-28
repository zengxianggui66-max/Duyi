package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.ResourceCatalogAliasQueryDTO;
import com.k12.common.dto.ResourceCatalogAliasUpsertDTO;
import com.k12.common.entity.ResourceCatalogAlias;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.ResourceCatalogAliasMapper;
import com.k12.resource.service.ResourceCatalogAliasService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@SuppressWarnings("null")
public class ResourceCatalogAliasServiceImpl implements ResourceCatalogAliasService {

    private static final String PERM_VIEW = "admin:resource:view";
    private static final String PERM_EDIT = "admin:resource:edit";

    private final ResourceCatalogAliasMapper aliasMapper;
    private final AdminPermissionService adminPermissionService;

    public ResourceCatalogAliasServiceImpl(ResourceCatalogAliasMapper aliasMapper,
                                           AdminPermissionService adminPermissionService) {
        this.aliasMapper = aliasMapper;
        this.adminPermissionService = adminPermissionService;
    }

    @Override
    public Page<ResourceCatalogAlias> list(ResourceCatalogAliasQueryDTO query, Long adminUserId) {
        requireView(adminUserId);
        ResourceCatalogAliasQueryDTO q = query != null ? query : new ResourceCatalogAliasQueryDTO();
        Page<ResourceCatalogAlias> page = new Page<>(
                q.getCurrent() != null ? q.getCurrent() : 1,
                q.getSize() != null ? q.getSize() : 20);
        LambdaQueryWrapper<ResourceCatalogAlias> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(q.getSourceType())) {
            wrapper.eq(ResourceCatalogAlias::getSourceType, q.getSourceType().trim().toLowerCase());
        }
        if (q.getStatus() != null) {
            wrapper.eq(ResourceCatalogAlias::getStatus, q.getStatus());
        }
        if (StringUtils.hasText(q.getKeyword())) {
            String kw = q.getKeyword().trim();
            wrapper.and(w -> w.like(ResourceCatalogAlias::getLegacyTitle, kw)
                    .or().like(ResourceCatalogAlias::getAliasTitle, kw)
                    .or().like(ResourceCatalogAlias::getNotes, kw));
        }
        wrapper.orderByDesc(ResourceCatalogAlias::getUpdateTime).orderByDesc(ResourceCatalogAlias::getId);
        return aliasMapper.selectPage(page, wrapper);
    }

    @Override
    public ResourceCatalogAlias upsert(ResourceCatalogAliasUpsertDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        if (dto == null) {
            throw new BusinessException(400, "请求体不能为空");
        }
        if (!StringUtils.hasText(dto.getSourceType())) {
            throw new BusinessException(400, "sourceType 不能为空");
        }
        if (!StringUtils.hasText(dto.getLegacyTitle())) {
            throw new BusinessException(400, "legacyTitle 不能为空");
        }
        if (dto.getCatalogNodeId() == null || dto.getCatalogNodeId() <= 0) {
            throw new BusinessException(400, "catalogNodeId 必须大于 0");
        }
        if (dto.getConfidence() != null && (dto.getConfidence() < 0 || dto.getConfidence() > 100)) {
            throw new BusinessException(400, "confidence 必须在 0-100");
        }

        String sourceType = dto.getSourceType().trim().toLowerCase();
        String legacyTitle = dto.getLegacyTitle().trim();
        ResourceCatalogAlias row = aliasMapper.selectOne(
                new LambdaQueryWrapper<ResourceCatalogAlias>()
                        .eq(ResourceCatalogAlias::getSourceType, sourceType)
                        .eq(ResourceCatalogAlias::getLegacyTitle, legacyTitle)
                        .last("LIMIT 1")
        );
        boolean creating = row == null;
        if (creating) {
            row = new ResourceCatalogAlias();
            row.setSourceType(sourceType);
            row.setLegacyTitle(legacyTitle);
            row.setCreateTime(LocalDateTime.now());
        }
        row.setAliasTitle(StringUtils.hasText(dto.getAliasTitle()) ? dto.getAliasTitle().trim() : legacyTitle);
        row.setCatalogNodeId(dto.getCatalogNodeId());
        row.setConfidence(dto.getConfidence() != null ? dto.getConfidence() : 100);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        row.setNotes(dto.getNotes() != null ? dto.getNotes().trim() : null);
        row.setUpdateTime(LocalDateTime.now());
        if (creating) {
            aliasMapper.insert(row);
        } else {
            aliasMapper.updateById(row);
        }
        return row;
    }

    private void requireView(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_VIEW)) {
            throw new BusinessException(403, "无别名查看权限");
        }
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无别名编辑权限");
        }
    }
}
