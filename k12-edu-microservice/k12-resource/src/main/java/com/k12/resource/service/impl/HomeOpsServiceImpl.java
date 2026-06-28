package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.HomeBanner;
import com.k12.common.entity.HomeHotWord;
import com.k12.common.entity.HomeQuickEntry;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.HomeBannerMapper;
import com.k12.resource.mapper.HomeHotWordMapper;
import com.k12.resource.mapper.HomeQuickEntryMapper;
import com.k12.resource.service.HomeOpsService;
import com.k12.resource.util.HomeNavTargetValidator;
import com.k12.resource.util.HomePanelJsonHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings("null")
public class HomeOpsServiceImpl implements HomeOpsService {

    private static final String PERM_EDIT = "admin:home:edit";

    private final HomeBannerMapper homeBannerMapper;
    private final HomeQuickEntryMapper homeQuickEntryMapper;
    private final HomeHotWordMapper homeHotWordMapper;
    private final AdminPermissionService adminPermissionService;
    public HomeOpsServiceImpl(HomeBannerMapper homeBannerMapper, HomeQuickEntryMapper homeQuickEntryMapper, HomeHotWordMapper homeHotWordMapper, AdminPermissionService adminPermissionService) {
        this.homeBannerMapper = homeBannerMapper;
        this.homeQuickEntryMapper = homeQuickEntryMapper;
        this.homeHotWordMapper = homeHotWordMapper;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public List<HomeBannerVO> listBanners(String slotCode, String stage, boolean publicOnly) {
        LambdaQueryWrapper<HomeBanner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeBanner::getSlotCode, StringUtils.hasText(slotCode) ? slotCode : "home_hero");
        if (publicOnly) {
            wrapper.eq(HomeBanner::getStatus, 1);
        }
        wrapper.orderByDesc(HomeBanner::getSort).orderByDesc(HomeBanner::getId);
        List<HomeBanner> rows = homeBannerMapper.selectList(wrapper);
        List<HomeBannerVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (HomeBanner row : rows) {
            if (publicOnly && !matchesStage(row.getStageKeys(), stage)) {
                continue;
            }
            if (publicOnly && !withinSchedule(row.getStartTime(), row.getEndTime(), now)) {
                continue;
            }
            HomeBannerVO vo = mapBanner(row, !publicOnly);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<HomeQuickEntryVO> listQuickEntries(String stage, boolean publicOnly) {
        LambdaQueryWrapper<HomeQuickEntry> wrapper = new LambdaQueryWrapper<>();
        if (publicOnly) {
            wrapper.eq(HomeQuickEntry::getStatus, 1);
        }
        wrapper.orderByDesc(HomeQuickEntry::getSort).orderByDesc(HomeQuickEntry::getId);
        List<HomeQuickEntryVO> result = new ArrayList<>();
        for (HomeQuickEntry row : homeQuickEntryMapper.selectList(wrapper)) {
            if (publicOnly && !matchesStage(row.getStageKeys(), stage)) {
                continue;
            }
            HomeQuickEntryVO vo = mapQuickEntry(row, !publicOnly);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public List<HomeHotWordVO> listHotWords(String stage, boolean publicOnly) {
        LambdaQueryWrapper<HomeHotWord> wrapper = new LambdaQueryWrapper<>();
        if (publicOnly) {
            wrapper.eq(HomeHotWord::getStatus, 1);
        }
        wrapper.orderByDesc(HomeHotWord::getSort).orderByDesc(HomeHotWord::getId);
        List<HomeHotWordVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (HomeHotWord row : homeHotWordMapper.selectList(wrapper)) {
            if (publicOnly && !matchesStage(row.getStageKeys(), stage)) {
                continue;
            }
            if (publicOnly && !withinSchedule(row.getStartTime(), row.getEndTime(), now)) {
                continue;
            }
            HomeHotWordVO vo = mapHotWord(row, !publicOnly);
            if (vo != null) {
                result.add(vo);
            }
        }
        return result;
    }

    @Override
    public HomeHeroVO getHero(String slotCode, String stage) {
        HomeHeroVO hero = new HomeHeroVO();
        hero.setBanners(listBanners(slotCode, stage, true));
        hero.setQuickEntries(listQuickEntries(stage, true));
        hero.setHotWords(listHotWords(stage, true));
        return hero;
    }

    @Override
    @Transactional
    public HomeBannerVO createBanner(HomeBannerWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeNavTargetValidator.validate(dto.getNavTarget());
        HomeBanner row = new HomeBanner();
        applyBanner(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        homeBannerMapper.insert(row);
        return mapBanner(row, true);
    }

    @Override
    @Transactional
    public HomeBannerVO updateBanner(Long id, HomeBannerWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeBanner row = requireBanner(id);
        HomeNavTargetValidator.validate(dto.getNavTarget());
        applyBanner(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        homeBannerMapper.updateById(row);
        return mapBanner(row, true);
    }

    @Override
    @Transactional
    public void updateBannerStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomeBanner row = requireBanner(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        homeBannerMapper.updateById(row);
    }

    @Override
    @Transactional
    public void deleteBanner(Long id, Long adminUserId) {
        requireEdit(adminUserId);
        requireBanner(id);
        homeBannerMapper.deleteById(id);
    }

    @Override
    @Transactional
    public HomeQuickEntryVO createQuickEntry(HomeQuickEntryWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeNavTargetValidator.validate(dto.getNavTarget());
        assertUniqueEntryKey(dto.getEntryKey(), null);
        HomeQuickEntry row = new HomeQuickEntry();
        applyQuickEntry(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        homeQuickEntryMapper.insert(row);
        return mapQuickEntry(row, true);
    }

    @Override
    @Transactional
    public HomeQuickEntryVO updateQuickEntry(Long id, HomeQuickEntryWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeQuickEntry row = requireQuickEntry(id);
        HomeNavTargetValidator.validate(dto.getNavTarget());
        assertUniqueEntryKey(dto.getEntryKey(), id);
        applyQuickEntry(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        homeQuickEntryMapper.updateById(row);
        return mapQuickEntry(row, true);
    }

    @Override
    @Transactional
    public void updateQuickEntryStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomeQuickEntry row = requireQuickEntry(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        homeQuickEntryMapper.updateById(row);
    }

    @Override
    @Transactional
    public void deleteQuickEntry(Long id, Long adminUserId) {
        requireEdit(adminUserId);
        requireQuickEntry(id);
        homeQuickEntryMapper.deleteById(id);
    }

    @Override
    @Transactional
    public HomeHotWordVO createHotWord(HomeHotWordWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        validateHotWordWrite(dto);
        assertUniqueHotWordLabel(dto.getLabel(), null);
        HomeHotWord row = new HomeHotWord();
        applyHotWord(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        homeHotWordMapper.insert(row);
        return mapHotWord(row, true);
    }

    @Override
    @Transactional
    public HomeHotWordVO updateHotWord(Long id, HomeHotWordWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        validateHotWordWrite(dto);
        assertUniqueHotWordLabel(dto.getLabel(), id);
        HomeHotWord row = requireHotWord(id);
        applyHotWord(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        homeHotWordMapper.updateById(row);
        return mapHotWord(row, true);
    }

    @Override
    @Transactional
    public void updateHotWordStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomeHotWord row = requireHotWord(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        homeHotWordMapper.updateById(row);
    }

    @Override
    @Transactional
    public void deleteHotWord(Long id, Long adminUserId) {
        requireEdit(adminUserId);
        requireHotWord(id);
        homeHotWordMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void reorderHotWords(HomeHotWordReorderDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException(400, "排序列表不能为空");
        }
        LocalDateTime now = LocalDateTime.now();
        for (HomeHotWordReorderDTO.Item item : dto.getItems()) {
            if (item.getId() == null) {
                throw new BusinessException(400, "排序项 id 不能为空");
            }
            HomeHotWord row = requireHotWord(item.getId());
            row.setSort(item.getSort() != null ? item.getSort() : 0);
            row.setUpdateTime(now);
            homeHotWordMapper.updateById(row);
        }
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无首页配置编辑权限");
        }
    }

    private HomeBanner requireBanner(Long id) {
        HomeBanner row = homeBannerMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "轮播不存在");
        }
        return row;
    }

    private HomeQuickEntry requireQuickEntry(Long id) {
        HomeQuickEntry row = homeQuickEntryMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "快捷入口不存在");
        }
        return row;
    }

    private HomeHotWord requireHotWord(Long id) {
        HomeHotWord row = homeHotWordMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "热门词不存在");
        }
        return row;
    }

    private void assertUniqueEntryKey(String entryKey, Long excludeId) {
        LambdaQueryWrapper<HomeQuickEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeQuickEntry::getEntryKey, entryKey.trim());
        if (excludeId != null) {
            wrapper.ne(HomeQuickEntry::getId, excludeId);
        }
        if (homeQuickEntryMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "entry_key 已存在");
        }
    }

    private void assertUniqueHotWordLabel(String label, Long excludeId) {
        LambdaQueryWrapper<HomeHotWord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeHotWord::getLabel, label.trim());
        if (excludeId != null) {
            wrapper.ne(HomeHotWord::getId, excludeId);
        }
        if (homeHotWordMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "热门词文案已存在");
        }
    }

    private void validateHotWordWrite(HomeHotWordWriteDTO dto) {
        String actionType = dto.getActionType() != null ? dto.getActionType().trim() : "";
        if (!"browse".equals(actionType) && !"search".equals(actionType)) {
            throw new BusinessException(400, "action_type 仅支持 browse 或 search");
        }
        NavTargetDTO target = HomeNavTargetValidator.validate(dto.getNavTarget());
        if (!actionType.equals(target.getType())) {
            throw new BusinessException(400, "action_type 须与 nav_target.type 一致");
        }
    }

    private void applyBanner(HomeBanner row, HomeBannerWriteDTO dto) {
        row.setSlotCode(StringUtils.hasText(dto.getSlotCode()) ? dto.getSlotCode().trim() : "home_hero");
        row.setTitle(dto.getTitle().trim());
        row.setSubtitle(trimOrNull(dto.getSubtitle()));
        row.setCtaText(trimOrNull(dto.getCtaText()));
        row.setIcon(trimOrNull(dto.getIcon()));
        row.setBgColorStart(StringUtils.hasText(dto.getBgColorStart()) ? dto.getBgColorStart() : "#667EEA");
        row.setBgColorEnd(StringUtils.hasText(dto.getBgColorEnd()) ? dto.getBgColorEnd() : "#764BA2");
        row.setImageUrl(trimOrNull(dto.getImageUrl()));
        row.setNavTarget(HomeNavTargetValidator.toJson(dto.getNavTarget()));
        row.setStageKeys(HomeNavTargetValidator.stageKeysToJson(dto.getStageKeys()));
        row.setStartTime(dto.getStartTime());
        row.setEndTime(dto.getEndTime());
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private void applyQuickEntry(HomeQuickEntry row, HomeQuickEntryWriteDTO dto) {
        row.setEntryKey(dto.getEntryKey().trim());
        row.setTitle(dto.getTitle().trim());
        row.setDescription(trimOrNull(dto.getDescription()));
        row.setIcon(trimOrNull(dto.getIcon()));
        row.setAccentColor(StringUtils.hasText(dto.getAccentColor()) ? dto.getAccentColor() : "#4facfe");
        row.setNavTarget(HomeNavTargetValidator.toJson(dto.getNavTarget()));
        row.setStageKeys(HomeNavTargetValidator.stageKeysToJson(dto.getStageKeys()));
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private void applyHotWord(HomeHotWord row, HomeHotWordWriteDTO dto) {
        row.setLabel(dto.getLabel().trim());
        row.setActionType(dto.getActionType().trim());
        row.setNavTarget(HomeNavTargetValidator.toJson(dto.getNavTarget()));
        row.setBadge(trimOrNull(dto.getBadge()));
        row.setStageKeys(HomeNavTargetValidator.stageKeysToJson(dto.getStageKeys()));
        row.setStartTime(dto.getStartTime());
        row.setEndTime(dto.getEndTime());
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private HomeBannerVO mapBanner(HomeBanner row, boolean includeInvalid) {
        NavTargetDTO navTarget = HomeNavTargetValidator.parseLenient(row.getNavTarget());
        if (navTarget == null) {
            if (includeInvalid) {
                log.warn("home banner id={} has invalid nav_target, admin only", row.getId());
            } else {
                log.warn("home banner id={} skipped: invalid nav_target", row.getId());
                return null;
            }
        }
        HomeBannerVO vo = new HomeBannerVO();
        vo.setId(row.getId());
        vo.setSlotCode(row.getSlotCode());
        vo.setTitle(row.getTitle());
        vo.setSubtitle(row.getSubtitle());
        vo.setCtaText(row.getCtaText());
        vo.setIcon(row.getIcon());
        vo.setBgColorStart(row.getBgColorStart());
        vo.setBgColorEnd(row.getBgColorEnd());
        vo.setImageUrl(row.getImageUrl());
        vo.setNavTarget(navTarget);
        vo.setStageKeys(HomePanelJsonHelper.parseStringList(row.getStageKeys()));
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        return vo;
    }

    private HomeQuickEntryVO mapQuickEntry(HomeQuickEntry row, boolean includeInvalid) {
        NavTargetDTO navTarget = HomeNavTargetValidator.parseLenient(row.getNavTarget());
        if (navTarget == null) {
            if (includeInvalid) {
                log.warn("home quick entry id={} has invalid nav_target, admin only", row.getId());
            } else {
                log.warn("home quick entry id={} skipped: invalid nav_target", row.getId());
                return null;
            }
        }
        HomeQuickEntryVO vo = new HomeQuickEntryVO();
        vo.setId(row.getId());
        vo.setEntryKey(row.getEntryKey());
        vo.setTitle(row.getTitle());
        vo.setDescription(row.getDescription());
        vo.setIcon(row.getIcon());
        vo.setAccentColor(row.getAccentColor());
        vo.setNavTarget(navTarget);
        vo.setStageKeys(HomePanelJsonHelper.parseStringList(row.getStageKeys()));
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private HomeHotWordVO mapHotWord(HomeHotWord row, boolean includeInvalid) {
        NavTargetDTO navTarget = HomeNavTargetValidator.parseLenient(row.getNavTarget());
        if (navTarget == null) {
            if (includeInvalid) {
                log.warn("home hot word id={} has invalid nav_target, admin only", row.getId());
            } else {
                log.warn("home hot word id={} skipped: invalid nav_target", row.getId());
                return null;
            }
        }
        HomeHotWordVO vo = new HomeHotWordVO();
        vo.setId(row.getId());
        vo.setLabel(row.getLabel());
        vo.setActionType(row.getActionType());
        vo.setBadge(row.getBadge());
        vo.setNavTarget(navTarget);
        vo.setStageKeys(HomePanelJsonHelper.parseStringList(row.getStageKeys()));
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        vo.setStartTime(row.getStartTime());
        vo.setEndTime(row.getEndTime());
        return vo;
    }

    private boolean matchesStage(String stageKeysJson, String stage) {
        List<String> keys = HomePanelJsonHelper.parseStringList(stageKeysJson);
        if (keys.isEmpty()) {
            return true;
        }
        if (!StringUtils.hasText(stage)) {
            return true;
        }
        return keys.contains(stage.trim());
    }

    private boolean withinSchedule(LocalDateTime start, LocalDateTime end, LocalDateTime now) {
        if (start != null && now.isBefore(start)) {
            return false;
        }
        if (end != null && now.isAfter(end)) {
            return false;
        }
        return true;
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
