package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.HomeFuncChannelAdminVO;
import com.k12.common.dto.HomeFuncChannelWriteDTO;
import com.k12.common.entity.HomeFuncChannel;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.HomeFuncChannelMapper;
import com.k12.resource.service.AdminHomeFuncChannelService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@SuppressWarnings("null")
public class AdminHomeFuncChannelServiceImpl implements AdminHomeFuncChannelService {

    private static final String PERM_EDIT = "admin:home:edit";

    private final HomeFuncChannelMapper funcChannelMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminHomeFuncChannelServiceImpl(HomeFuncChannelMapper funcChannelMapper, AdminPermissionService adminPermissionService) {
        this.funcChannelMapper = funcChannelMapper;
        this.adminPermissionService = adminPermissionService;
    }


    @Override
    public List<HomeFuncChannelAdminVO> list(boolean includeDisabled) {
        LambdaQueryWrapper<HomeFuncChannel> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(HomeFuncChannel::getStatus, 1);
        }
        wrapper.orderByAsc(HomeFuncChannel::getSort).orderByAsc(HomeFuncChannel::getId);
        List<HomeFuncChannelAdminVO> result = new ArrayList<>();
        for (HomeFuncChannel row : funcChannelMapper.selectList(wrapper)) {
            result.add(map(row));
        }
        return result;
    }

    @Override
    @Transactional
    public HomeFuncChannelAdminVO update(Long id, HomeFuncChannelWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeFuncChannel row = require(id);
        apply(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        funcChannelMapper.updateById(row);
        return map(row);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomeFuncChannel row = require(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        funcChannelMapper.updateById(row);
    }

    private void apply(HomeFuncChannel row, HomeFuncChannelWriteDTO dto) {
        row.setName(dto.getName().trim());
        row.setExamType(dto.getExamType().trim());
        row.setDefaultTopic(dto.getDefaultTopic().trim());
        row.setStageKey(dto.getStageKey().trim());
        row.setPaperTab(dto.getPaperTab().trim());
        row.setPaperDefaultGrade(dto.getPaperDefaultGrade().trim());
        row.setScrollTarget(StringUtils.hasText(dto.getScrollTarget()) ? dto.getScrollTarget().trim() : "exam-module");
        row.setExamTabLabel(trimOrNull(dto.getExamTabLabel()));
        row.setBrowseModule(trimOrNull(dto.getBrowseModule()));
        row.setBrowseStageKey(trimOrNull(dto.getBrowseStageKey()));
        row.setBrowseDefaultVolume(trimOrNull(dto.getBrowseDefaultVolume()));
        row.setSort(dto.getSort() != null ? dto.getSort() : row.getSort());
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : row.getStatus());
    }

    private HomeFuncChannelAdminVO map(HomeFuncChannel row) {
        HomeFuncChannelAdminVO vo = new HomeFuncChannelAdminVO();
        vo.setId(row.getId());
        vo.setFuncKey(row.getFuncKey());
        vo.setName(row.getName());
        vo.setExamType(row.getExamType());
        vo.setDefaultTopic(row.getDefaultTopic());
        vo.setStageKey(row.getStageKey());
        vo.setPaperTab(row.getPaperTab());
        vo.setPaperDefaultGrade(row.getPaperDefaultGrade());
        vo.setScrollTarget(row.getScrollTarget());
        vo.setExamTabLabel(row.getExamTabLabel());
        vo.setBrowseModule(row.getBrowseModule());
        vo.setBrowseStageKey(row.getBrowseStageKey());
        vo.setBrowseDefaultVolume(row.getBrowseDefaultVolume());
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        return vo;
    }

    private HomeFuncChannel require(Long id) {
        HomeFuncChannel row = funcChannelMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "升学入口不存在");
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
