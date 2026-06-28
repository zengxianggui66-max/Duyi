package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.HomePanelFeatured;
import com.k12.common.entity.HomePanelTabConfig;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.HomePanelFeaturedMapper;
import com.k12.resource.mapper.HomePanelResourceMapper;
import com.k12.resource.mapper.HomePanelTabConfigMapper;
import com.k12.resource.service.AdminHomePanelService;
import com.k12.resource.service.HomePanelService;
import com.k12.resource.util.HomePanelJsonHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SuppressWarnings("null")
public class AdminHomePanelServiceImpl implements AdminHomePanelService {

    private static final String PERM_EDIT = "admin:home:edit";
    private static final Set<String> PANEL_CODES = Set.of("sync_prep", "paper_zone", "promotion");
    private static final Set<String> RESOURCE_SOURCES = Set.of(
            "edu_resource", "oss_primary_chinese", "edu_resource_suite");

    private final HomePanelTabConfigMapper tabConfigMapper;
    private final HomePanelFeaturedMapper featuredMapper;
    private final HomePanelResourceMapper resourceMapper;
    private final HomePanelService homePanelService;
    private final AdminPermissionService adminPermissionService;
    public AdminHomePanelServiceImpl(HomePanelTabConfigMapper tabConfigMapper, HomePanelFeaturedMapper featuredMapper, HomePanelResourceMapper resourceMapper, HomePanelService homePanelService, AdminPermissionService adminPermissionService) {
        this.tabConfigMapper = tabConfigMapper;
        this.featuredMapper = featuredMapper;
        this.resourceMapper = resourceMapper;
        this.homePanelService = homePanelService;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public List<HomePanelTabConfigVO> listTabConfigs(String panelCode, boolean includeDisabled) {
        LambdaQueryWrapper<HomePanelTabConfig> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(panelCode)) {
            wrapper.eq(HomePanelTabConfig::getPanelCode, panelCode.trim());
        }
        if (!includeDisabled) {
            wrapper.eq(HomePanelTabConfig::getStatus, 1);
        }
        wrapper.orderByAsc(HomePanelTabConfig::getPanelCode)
                .orderByAsc(HomePanelTabConfig::getSort)
                .orderByAsc(HomePanelTabConfig::getId);
        List<HomePanelTabConfigVO> result = new ArrayList<>();
        for (HomePanelTabConfig row : tabConfigMapper.selectList(wrapper)) {
            result.add(mapTabConfig(row));
        }
        return result;
    }

    @Override
    @Transactional
    public HomePanelTabConfigVO updateTabConfig(Long id, HomePanelTabConfigWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomePanelTabConfig row = requireTabConfig(id);
        applyTabConfig(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        tabConfigMapper.updateById(row);
        return mapTabConfig(row);
    }

    @Override
    @Transactional
    public void updateTabConfigStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomePanelTabConfig row = requireTabConfig(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        tabConfigMapper.updateById(row);
    }

    @Override
    public List<HomePanelFeaturedVO> listFeatured(
            String panelCode, String tabKey, String filterKey, boolean includeDisabled) {
        LambdaQueryWrapper<HomePanelFeatured> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(panelCode)) {
            wrapper.eq(HomePanelFeatured::getPanelCode, panelCode.trim());
        }
        if (StringUtils.hasText(tabKey)) {
            wrapper.eq(HomePanelFeatured::getTabKey, tabKey.trim());
        }
        if (StringUtils.hasText(filterKey)) {
            wrapper.eq(HomePanelFeatured::getFilterKey, filterKey.trim());
        }
        if (!includeDisabled) {
            wrapper.eq(HomePanelFeatured::getStatus, 1);
        }
        wrapper.orderByDesc(HomePanelFeatured::getSort).orderByDesc(HomePanelFeatured::getId);
        List<HomePanelFeaturedVO> result = new ArrayList<>();
        for (HomePanelFeatured row : featuredMapper.selectList(wrapper)) {
            result.add(mapFeatured(row));
        }
        return result;
    }

    @Override
    @Transactional
    public HomePanelFeaturedVO createFeatured(HomePanelFeaturedWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        validateFeaturedWrite(dto);
        validateResourceAvailable(dto.getResourceSource(), dto.getResourceId());
        HomePanelFeatured row = new HomePanelFeatured();
        applyFeatured(row, dto);
        featuredMapper.insert(row);
        return mapFeatured(row);
    }

    @Override
    @Transactional
    public HomePanelFeaturedVO updateFeatured(Long id, HomePanelFeaturedWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomePanelFeatured row = requireFeatured(id);
        validateFeaturedWrite(dto);
        validateResourceAvailable(dto.getResourceSource(), dto.getResourceId());
        applyFeatured(row, dto);
        featuredMapper.updateById(row);
        return mapFeatured(row);
    }

    @Override
    @Transactional
    public void updateFeaturedStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomePanelFeatured row = requireFeatured(id);
        row.setStatus(status);
        featuredMapper.updateById(row);
    }

    @Override
    @Transactional
    public void deleteFeatured(Long id, Long adminUserId) {
        requireEdit(adminUserId);
        requireFeatured(id);
        featuredMapper.deleteById(id);
    }

    @Override
    public HomePanelListVO previewPanel(
            String panelCode,
            String tabKey,
            String filterKey,
            String stageKey,
            String subjectName,
            String gradeName,
            Integer limit) {
        if (!StringUtils.hasText(panelCode) || !StringUtils.hasText(tabKey)) {
            throw new BusinessException(400, "panelCode 与 tabKey 不能为空");
        }
        return switch (panelCode.trim()) {
            case "sync_prep" -> homePanelService.listSyncPrep(
                    stageKey != null ? stageKey : "primary",
                    subjectName,
                    tabKey,
                    limit);
            case "paper_zone" -> homePanelService.listPaperZone(
                    stageKey != null ? stageKey : "primary",
                    gradeName,
                    tabKey,
                    limit);
            case "promotion" -> homePanelService.listPromotion(
                    tabKey,
                    filterKey,
                    limit);
            default -> throw new BusinessException(400, "不支持的 panelCode: " + panelCode);
        };
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无首页配置编辑权限");
        }
    }

    private HomePanelTabConfig requireTabConfig(Long id) {
        HomePanelTabConfig row = tabConfigMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "Tab 配置不存在");
        }
        return row;
    }

    private HomePanelFeatured requireFeatured(Long id) {
        HomePanelFeatured row = featuredMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "置顶推荐不存在");
        }
        return row;
    }

    private void validateFeaturedWrite(HomePanelFeaturedWriteDTO dto) {
        if (!PANEL_CODES.contains(dto.getPanelCode())) {
            throw new BusinessException(400, "panelCode 无效");
        }
        if (!RESOURCE_SOURCES.contains(dto.getResourceSource())) {
            throw new BusinessException(400, "resourceSource 无效");
        }
    }

    private void validateResourceAvailable(String resourceSource, Long resourceId) {
        Map<String, Object> row = switch (resourceSource) {
            case "edu_resource" -> resourceMapper.findEduById(resourceId);
            case "oss_primary_chinese" -> resourceMapper.findOssById(resourceId);
            case "edu_resource_suite" -> resourceMapper.findSuiteById(resourceId);
            default -> null;
        };
        if (row == null || row.get("title") == null) {
            throw new BusinessException(400, "资源不存在或未上架，无法置顶");
        }
    }

    private void applyTabConfig(HomePanelTabConfig row, HomePanelTabConfigWriteDTO dto) {
        if (dto.getTabLabel() != null) {
            row.setTabLabel(dto.getTabLabel().trim());
        }
        if (dto.getModuleNames() != null) {
            row.setModuleNames(HomePanelJsonHelper.toJsonStringList(dto.getModuleNames()));
        }
        if (dto.getExcludeModuleNames() != null) {
            row.setExcludeModuleNames(HomePanelJsonHelper.toJsonStringList(dto.getExcludeModuleNames()));
        }
        if (dto.getResourceTypeNames() != null) {
            row.setResourceTypeNames(HomePanelJsonHelper.toJsonStringList(dto.getResourceTypeNames()));
        }
        if (dto.getTitleKeyword() != null) {
            row.setTitleKeyword(StringUtils.hasText(dto.getTitleKeyword()) ? dto.getTitleKeyword().trim() : null);
        }
        if (StringUtils.hasText(dto.getQueryMode())) {
            row.setQueryMode(dto.getQueryMode().trim());
        }
        if (dto.getSort() != null) {
            row.setSort(dto.getSort());
        }
        if (dto.getStatus() != null) {
            row.setStatus(dto.getStatus());
        }
    }

    private void applyFeatured(HomePanelFeatured row, HomePanelFeaturedWriteDTO dto) {
        row.setPanelCode(dto.getPanelCode().trim());
        row.setTabKey(dto.getTabKey().trim());
        row.setFilterKey(trimOrNull(dto.getFilterKey()));
        row.setStageKey(trimOrNull(dto.getStageKey()));
        row.setSubjectName(trimOrNull(dto.getSubjectName()));
        row.setGradeName(trimOrNull(dto.getGradeName()));
        row.setResourceId(dto.getResourceId());
        row.setResourceSource(dto.getResourceSource().trim());
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setExpireTime(dto.getExpireTime());
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
    }

    private HomePanelTabConfigVO mapTabConfig(HomePanelTabConfig row) {
        HomePanelTabConfigVO vo = new HomePanelTabConfigVO();
        vo.setId(row.getId());
        vo.setPanelCode(row.getPanelCode());
        vo.setTabKey(row.getTabKey());
        vo.setFilterKey(row.getFilterKey());
        vo.setTabLabel(row.getTabLabel());
        vo.setModuleNames(HomePanelJsonHelper.parseStringList(row.getModuleNames()));
        vo.setExcludeModuleNames(HomePanelJsonHelper.parseStringList(row.getExcludeModuleNames()));
        vo.setResourceTypeNames(HomePanelJsonHelper.parseStringList(row.getResourceTypeNames()));
        vo.setTitleKeyword(row.getTitleKeyword());
        vo.setQueryMode(row.getQueryMode());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    private HomePanelFeaturedVO mapFeatured(HomePanelFeatured row) {
        HomePanelFeaturedVO vo = new HomePanelFeaturedVO();
        vo.setId(row.getId());
        vo.setPanelCode(row.getPanelCode());
        vo.setTabKey(row.getTabKey());
        vo.setFilterKey(row.getFilterKey());
        vo.setStageKey(row.getStageKey());
        vo.setSubjectName(row.getSubjectName());
        vo.setGradeName(row.getGradeName());
        vo.setResourceId(row.getResourceId());
        vo.setResourceSource(row.getResourceSource());
        vo.setSort(row.getSort());
        vo.setExpireTime(row.getExpireTime());
        vo.setStatus(row.getStatus());
        vo.setResourceTitle(resolveResourceTitle(row.getResourceSource(), row.getResourceId()));
        return vo;
    }

    private String resolveResourceTitle(String source, Long resourceId) {
        if (resourceId == null || !StringUtils.hasText(source)) {
            return null;
        }
        Map<String, Object> row = switch (source) {
            case "edu_resource" -> resourceMapper.findEduById(resourceId);
            case "oss_primary_chinese" -> resourceMapper.findOssById(resourceId);
            case "edu_resource_suite" -> resourceMapper.findSuiteById(resourceId);
            default -> null;
        };
        return row != null && row.get("title") != null ? String.valueOf(row.get("title")) : null;
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
