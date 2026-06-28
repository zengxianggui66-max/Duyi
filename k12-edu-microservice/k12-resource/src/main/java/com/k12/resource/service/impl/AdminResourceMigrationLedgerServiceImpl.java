package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.ResourceMigrationLedgerUpdateDTO;
import com.k12.common.entity.ResourceMigrationLedger;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.ResourceMigrationLedgerMapper;
import com.k12.resource.service.AdminResourceMigrationLedgerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@SuppressWarnings("null")
public class AdminResourceMigrationLedgerServiceImpl implements AdminResourceMigrationLedgerService {

    private static final String PERM_VIEW = "admin:resource:view";
    private static final String PERM_EDIT = "admin:resource:edit";

    private final ResourceMigrationLedgerMapper ledgerMapper;
    private final AdminPermissionService adminPermissionService;

    public AdminResourceMigrationLedgerServiceImpl(ResourceMigrationLedgerMapper ledgerMapper,
                                                   AdminPermissionService adminPermissionService) {
        this.ledgerMapper = ledgerMapper;
        this.adminPermissionService = adminPermissionService;
    }

    @Override
    public List<ResourceMigrationLedger> list(Long adminUserId) {
        requireView(adminUserId);
        return ledgerMapper.selectList(new LambdaQueryWrapper<ResourceMigrationLedger>()
                .orderByAsc(ResourceMigrationLedger::getSourceType));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResourceMigrationLedger upsert(String sourceType, ResourceMigrationLedgerUpdateDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        String normalizedSourceType = normalizeSourceType(sourceType);
        if (dto == null) {
            throw new BusinessException(400, "更新内容不能为空");
        }
        validateStatus("backfillStatus", dto.getBackfillStatus());
        validateStatus("frontendSwitchStatus", dto.getFrontendSwitchStatus());
        validateStatus("legacyApiOfflineStatus", dto.getLegacyApiOfflineStatus());
        validateCompareStatus(dto.getCompareStatus());
        validatePassRate(dto.getComparePassRate());

        ResourceMigrationLedger existing = ledgerMapper.selectOne(new LambdaQueryWrapper<ResourceMigrationLedger>()
                .eq(ResourceMigrationLedger::getSourceType, normalizedSourceType)
                .last("LIMIT 1"));
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            existing = new ResourceMigrationLedger();
            existing.setSourceType(normalizedSourceType);
            existing.setCreateTime(now);
            applyPatch(existing, dto);
            existing.setUpdateTime(now);
            ledgerMapper.insert(existing);
            return existing;
        }
        applyPatch(existing, dto);
        existing.setUpdateTime(now);
        ledgerMapper.updateById(existing);
        return existing;
    }

    private void applyPatch(ResourceMigrationLedger row, ResourceMigrationLedgerUpdateDTO dto) {
        if (StringUtils.hasText(dto.getLegacySourceTable())) {
            row.setLegacySourceTable(dto.getLegacySourceTable().trim());
        }
        if (StringUtils.hasText(dto.getTargetMainChain())) {
            row.setTargetMainChain(dto.getTargetMainChain().trim());
        }
        if (dto.getBackfillStatus() != null) {
            row.setBackfillStatus(dto.getBackfillStatus());
        }
        if (dto.getFrontendSwitchStatus() != null) {
            row.setFrontendSwitchStatus(dto.getFrontendSwitchStatus());
        }
        if (dto.getLegacyApiOfflineStatus() != null) {
            row.setLegacyApiOfflineStatus(dto.getLegacyApiOfflineStatus());
        }
        if (dto.getCompareStatus() != null) {
            row.setCompareStatus(dto.getCompareStatus());
        }
        if (dto.getComparePassRate() != null) {
            row.setComparePassRate(dto.getComparePassRate());
        }
        if (dto.getNotes() != null) {
            row.setNotes(dto.getNotes().trim());
        }
        if (dto.getCompareStatus() != null || dto.getComparePassRate() != null) {
            row.setLastComparedAt(LocalDateTime.now());
        }
    }

    private String normalizeSourceType(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            throw new BusinessException(400, "sourceType 不能为空");
        }
        return sourceType.trim().toLowerCase();
    }

    private void validateStatus(String field, Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 2) {
            throw new BusinessException(400, field + " 仅支持 0/1/2");
        }
    }

    private void validateCompareStatus(Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 2) {
            throw new BusinessException(400, "compareStatus 仅支持 0/1/2");
        }
    }

    private void validatePassRate(Integer value) {
        if (value == null) {
            return;
        }
        if (value < 0 || value > 100) {
            throw new BusinessException(400, "comparePassRate 必须在 0-100");
        }
    }

    private void requireView(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_VIEW)) {
            throw new BusinessException(403, "无台账查看权限");
        }
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无台账编辑权限");
        }
    }
}
