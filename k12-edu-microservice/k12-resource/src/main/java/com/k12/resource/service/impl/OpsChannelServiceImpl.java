package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.OpsChannel;
import com.k12.common.entity.OpsChannelFeaturedAlbum;
import com.k12.common.entity.OpsChannelTab;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.OpsChannelFeaturedAlbumMapper;
import com.k12.resource.mapper.OpsChannelMapper;
import com.k12.resource.mapper.OpsChannelTabMapper;
import com.k12.resource.service.OpsChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@SuppressWarnings("null")
public class OpsChannelServiceImpl implements OpsChannelService {

    private static final String PERM_EDIT = "admin:home:edit";

    private final OpsChannelMapper channelMapper;
    private final OpsChannelTabMapper tabMapper;
    private final OpsChannelFeaturedAlbumMapper albumMapper;
    private final AdminPermissionService adminPermissionService;
    private final ObjectMapper objectMapper;
    public OpsChannelServiceImpl(OpsChannelMapper channelMapper, OpsChannelTabMapper tabMapper, OpsChannelFeaturedAlbumMapper albumMapper, AdminPermissionService adminPermissionService, ObjectMapper objectMapper) {
        this.channelMapper = channelMapper;
        this.tabMapper = tabMapper;
        this.albumMapper = albumMapper;
        this.adminPermissionService = adminPermissionService;
        this.objectMapper = objectMapper;
    }


    @Override
    public OpsChannelBootstrapVO getBootstrap(String code) {
        OpsChannel channel = requireByCode(code, false);
        return buildBootstrap(channel);
    }

    @Override
    public List<OpsChannelAdminVO> listAdmin(boolean includeDisabled) {
        LambdaQueryWrapper<OpsChannel> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(OpsChannel::getStatus, 1);
        }
        wrapper.orderByDesc(OpsChannel::getSort).orderByAsc(OpsChannel::getId);
        List<OpsChannelAdminVO> result = new ArrayList<>();
        for (OpsChannel row : channelMapper.selectList(wrapper)) {
            result.add(mapAdmin(row));
        }
        return result;
    }

    @Override
    @Transactional
    public OpsChannelAdminVO updateChannel(Long id, OpsChannelWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        OpsChannel row = requireChannel(id);
        applyChannel(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        channelMapper.updateById(row);
        return mapAdmin(row);
    }

    @Override
    @Transactional
    public void updateChannelStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        OpsChannel row = requireChannel(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        channelMapper.updateById(row);
    }

    @Override
    public List<OpsChannelTabVO> listTabs(String channelCode, boolean includeDisabled) {
        requireByCode(channelCode, true);
        LambdaQueryWrapper<OpsChannelTab> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpsChannelTab::getChannelCode, channelCode.trim());
        if (!includeDisabled) {
            wrapper.eq(OpsChannelTab::getStatus, 1);
        }
        wrapper.orderByDesc(OpsChannelTab::getSort).orderByAsc(OpsChannelTab::getId);
        List<OpsChannelTabVO> result = new ArrayList<>();
        for (OpsChannelTab row : tabMapper.selectList(wrapper)) {
            result.add(mapTab(row));
        }
        return result;
    }

    @Override
    @Transactional
    public OpsChannelTabVO updateTab(Long tabId, OpsChannelTabWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        OpsChannelTab row = requireTab(tabId);
        row.setTabName(dto.getTabName().trim());
        row.setIcon(trimOrNull(dto.getIcon()));
        row.setSearchKeyword(trimOrNull(dto.getSearchKeyword()));
        row.setSort(dto.getSort() != null ? dto.getSort() : row.getSort());
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : row.getStatus());
        row.setUpdateTime(LocalDateTime.now());
        tabMapper.updateById(row);
        return mapTab(row);
    }

    @Override
    public List<OpsChannelAlbumVO> listAlbums(String channelCode, boolean includeDisabled) {
        requireByCode(channelCode, true);
        LambdaQueryWrapper<OpsChannelFeaturedAlbum> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OpsChannelFeaturedAlbum::getChannelCode, channelCode.trim());
        if (!includeDisabled) {
            wrapper.eq(OpsChannelFeaturedAlbum::getStatus, 1);
        }
        wrapper.orderByDesc(OpsChannelFeaturedAlbum::getSort).orderByAsc(OpsChannelFeaturedAlbum::getId);
        List<OpsChannelAlbumVO> result = new ArrayList<>();
        for (OpsChannelFeaturedAlbum row : albumMapper.selectList(wrapper)) {
            result.add(mapAlbum(row));
        }
        return result;
    }

    @Override
    @Transactional
    public OpsChannelAlbumVO createAlbum(String channelCode, OpsChannelAlbumWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        requireByCode(channelCode, true);
        OpsChannelFeaturedAlbum row = new OpsChannelFeaturedAlbum();
        row.setChannelCode(channelCode.trim());
        applyAlbum(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        albumMapper.insert(row);
        return mapAlbum(row);
    }

    @Override
    @Transactional
    public OpsChannelAlbumVO updateAlbum(Long albumId, OpsChannelAlbumWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        OpsChannelFeaturedAlbum row = requireAlbum(albumId);
        applyAlbum(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        albumMapper.updateById(row);
        return mapAlbum(row);
    }

    @Override
    @Transactional
    public void deleteAlbum(Long albumId, Long adminUserId) {
        requireEdit(adminUserId);
        requireAlbum(albumId);
        albumMapper.deleteById(albumId);
    }

    private OpsChannelBootstrapVO buildBootstrap(OpsChannel channel) {
        OpsChannelBootstrapVO vo = new OpsChannelBootstrapVO();
        vo.setCode(channel.getCode());
        vo.setName(channel.getName());
        vo.setIcon(channel.getIcon());
        vo.setDesc(channel.getDescription());
        vo.setBgColor(channel.getBgGradient());
        vo.setStats(parseStats(channel.getStatsJson()));
        OpsChannelUiDTO ui = parseUi(channel.getUiJson());
        if (ui != null) {
            vo.setShowGradeFilter(ui.getShowGradeFilter());
            vo.setShowSubjectFilter(ui.getShowSubjectFilter());
            vo.setEliteTitle(ui.getEliteTitle());
            vo.setEliteDesc(ui.getEliteDesc());
        }

        List<OpsChannelTab> tabs = tabMapper.selectList(new LambdaQueryWrapper<OpsChannelTab>()
                .eq(OpsChannelTab::getChannelCode, channel.getCode())
                .eq(OpsChannelTab::getStatus, 1)
                .orderByDesc(OpsChannelTab::getSort)
                .orderByAsc(OpsChannelTab::getId));
        List<OpsChannelTabItemVO> mainTabs = new ArrayList<>();
        Map<String, String> tabKeywords = new LinkedHashMap<>();
        for (OpsChannelTab tab : tabs) {
            OpsChannelTabItemVO item = new OpsChannelTabItemVO();
            item.setKey(tab.getTabKey());
            item.setName(tab.getTabName());
            item.setIcon(tab.getIcon());
            mainTabs.add(item);
            if (StringUtils.hasText(tab.getSearchKeyword())) {
                tabKeywords.put(tab.getTabKey(), tab.getSearchKeyword());
            }
        }
        vo.setMainTabs(mainTabs);
        vo.setTabKeywords(tabKeywords);

        List<OpsChannelFeaturedAlbum> albums = albumMapper.selectList(new LambdaQueryWrapper<OpsChannelFeaturedAlbum>()
                .eq(OpsChannelFeaturedAlbum::getChannelCode, channel.getCode())
                .eq(OpsChannelFeaturedAlbum::getStatus, 1)
                .orderByDesc(OpsChannelFeaturedAlbum::getSort)
                .orderByAsc(OpsChannelFeaturedAlbum::getId));
        List<OpsChannelAlbumItemVO> eliteAlbums = new ArrayList<>();
        for (OpsChannelFeaturedAlbum album : albums) {
            OpsChannelAlbumItemVO item = new OpsChannelAlbumItemVO();
            item.setId(album.getId());
            item.setTitle(album.getTitle());
            item.setIcon(album.getIcon());
            item.setMeta(album.getMeta());
            item.setResourceCount(album.getResourceCount());
            item.setDownloadCount(album.getDownloadCount());
            item.setCoverColor(album.getCoverGradient());
            item.setLinkPath(album.getLinkPath());
            eliteAlbums.add(item);
        }
        vo.setEliteAlbums(eliteAlbums);
        return vo;
    }

    private OpsChannelAdminVO mapAdmin(OpsChannel row) {
        OpsChannelAdminVO vo = new OpsChannelAdminVO();
        vo.setId(row.getId());
        vo.setCode(row.getCode());
        vo.setName(row.getName());
        vo.setIcon(row.getIcon());
        vo.setDescription(row.getDescription());
        vo.setBgGradient(row.getBgGradient());
        vo.setRoutePath(row.getRoutePath());
        vo.setStats(parseStats(row.getStatsJson()));
        vo.setUi(parseUi(row.getUiJson()));
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private OpsChannelTabVO mapTab(OpsChannelTab row) {
        OpsChannelTabVO vo = new OpsChannelTabVO();
        vo.setId(row.getId());
        vo.setChannelCode(row.getChannelCode());
        vo.setTabKey(row.getTabKey());
        vo.setTabName(row.getTabName());
        vo.setIcon(row.getIcon());
        vo.setSearchKeyword(row.getSearchKeyword());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    private OpsChannelAlbumVO mapAlbum(OpsChannelFeaturedAlbum row) {
        OpsChannelAlbumVO vo = new OpsChannelAlbumVO();
        vo.setId(row.getId());
        vo.setChannelCode(row.getChannelCode());
        vo.setTitle(row.getTitle());
        vo.setIcon(row.getIcon());
        vo.setMeta(row.getMeta());
        vo.setResourceCount(row.getResourceCount());
        vo.setDownloadCount(row.getDownloadCount());
        vo.setCoverGradient(row.getCoverGradient());
        vo.setLinkPath(row.getLinkPath());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    private void applyChannel(OpsChannel row, OpsChannelWriteDTO dto) {
        row.setName(dto.getName().trim());
        row.setIcon(trimOrNull(dto.getIcon()));
        row.setDescription(trimOrNull(dto.getDescription()));
        row.setBgGradient(trimOrNull(dto.getBgGradient()));
        row.setRoutePath(trimOrNull(dto.getRoutePath()));
        row.setStatsJson(toJson(dto.getStats()));
        row.setUiJson(toJson(dto.getUi()));
        row.setSort(dto.getSort() != null ? dto.getSort() : row.getSort());
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : row.getStatus());
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private void applyAlbum(OpsChannelFeaturedAlbum row, OpsChannelAlbumWriteDTO dto) {
        row.setTitle(dto.getTitle().trim());
        row.setIcon(trimOrNull(dto.getIcon()));
        row.setMeta(trimOrNull(dto.getMeta()));
        row.setResourceCount(dto.getResourceCount() != null ? dto.getResourceCount() : 0);
        row.setDownloadCount(dto.getDownloadCount() != null ? dto.getDownloadCount() : 0);
        row.setCoverGradient(trimOrNull(dto.getCoverGradient()));
        row.setLinkPath(trimOrNull(dto.getLinkPath()));
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
    }

    private OpsChannelStatsDTO parseStats(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, OpsChannelStatsDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("invalid ops_channel stats_json: {}", json);
            return null;
        }
    }

    private OpsChannelUiDTO parseUi(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, OpsChannelUiDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("invalid ops_channel ui_json: {}", json);
            return null;
        }
    }

    private String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "JSON 配置无法序列化");
        }
    }

    private OpsChannel requireByCode(String code, boolean adminOk) {
        if (!StringUtils.hasText(code)) {
            throw new BusinessException(400, "频道 code 不能为空");
        }
        OpsChannel row = channelMapper.selectOne(new LambdaQueryWrapper<OpsChannel>()
                .eq(OpsChannel::getCode, code.trim()));
        if (row == null) {
            throw new BusinessException(404, "特色频道不存在");
        }
        if (!adminOk && row.getStatus() != 1) {
            throw new BusinessException(404, "特色频道已下线");
        }
        return row;
    }

    private OpsChannel requireChannel(Long id) {
        OpsChannel row = channelMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "特色频道不存在");
        }
        return row;
    }

    private OpsChannelTab requireTab(Long id) {
        OpsChannelTab row = tabMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "Tab 不存在");
        }
        return row;
    }

    private OpsChannelFeaturedAlbum requireAlbum(Long id) {
        OpsChannelFeaturedAlbum row = albumMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "精品专辑不存在");
        }
        return row;
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无首页配置编辑权限");
        }
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
