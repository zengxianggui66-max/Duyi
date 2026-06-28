package com.k12.resource.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.k12.common.constant.ResourceStatusConstants;

import com.k12.common.constant.UserResourceActionType;

import com.k12.common.dto.AdminUserActionVO;

import com.k12.common.dto.AdminUserLoginLogVO;

import com.k12.common.dto.AdminUserStatsVO;

import com.k12.common.dto.AdminUserUploadVO;

import com.k12.common.dto.CollectItemVO;

import com.k12.common.entity.PrimaryChineseResource;

import com.k12.common.entity.UserResourceAction;

import com.k12.resource.mapper.CollectionMapper;

import com.k12.resource.mapper.PrimaryChineseResourceMapper;

import com.k12.resource.mapper.UserLoginLogReadMapper;

import com.k12.resource.mapper.UserResourceActionMapper;

import com.k12.resource.service.AdminUserBehaviorService;

import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;



import java.util.List;

import java.util.stream.Collectors;



@Service
@SuppressWarnings("null")
public class AdminUserBehaviorServiceImpl implements AdminUserBehaviorService {



    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;

    private final CollectionMapper collectionMapper;

    private final UserResourceActionMapper userResourceActionMapper;

    private final UserLoginLogReadMapper userLoginLogReadMapper;
    public AdminUserBehaviorServiceImpl(PrimaryChineseResourceMapper primaryChineseResourceMapper, CollectionMapper collectionMapper, UserResourceActionMapper userResourceActionMapper, UserLoginLogReadMapper userLoginLogReadMapper) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.collectionMapper = collectionMapper;
        this.userResourceActionMapper = userResourceActionMapper;
        this.userLoginLogReadMapper = userLoginLogReadMapper;
    }




    @Override

    public AdminUserStatsVO getStats(Long userId) {

        AdminUserStatsVO vo = new AdminUserStatsVO();

        vo.setUploadCount(primaryChineseResourceMapper.countByUploader(userId));

        vo.setCollectionCount(collectionMapper.countByUser(userId));

        vo.setViewCount(userResourceActionMapper.countByUserAndType(userId, UserResourceActionType.VIEW));

        vo.setDownloadCount(userResourceActionMapper.countByUserAndType(userId, UserResourceActionType.DOWNLOAD));

        vo.setSearchCount(userResourceActionMapper.countByUserAndType(userId, UserResourceActionType.SEARCH));

        vo.setLoginCount(userLoginLogReadMapper.countByUser(userId));

        return vo;

    }



    @Override

    public Page<AdminUserUploadVO> listUploads(Long userId, int current, int size) {

        int page = Math.max(current, 1);

        int pageSize = Math.min(Math.max(size, 1), 50);

        Page<PrimaryChineseResource> pg = new Page<>(page, pageSize);

        IPage<PrimaryChineseResource> result = primaryChineseResourceMapper.adminFindPage(

                pg, null, null, null, null, null, null,

                null, null, null, null, null, userId, null, true,

                null, null, null, "upload_time", "DESC");

        Page<AdminUserUploadVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        voPage.setRecords(result.getRecords().stream().map(this::toUploadVo).collect(Collectors.toList()));

        return voPage;

    }



    @Override

    public Page<CollectItemVO> listCollections(Long userId, int current, int size) {

        int page = Math.max(current, 1);

        int pageSize = Math.min(Math.max(size, 1), 50);

        Page<CollectItemVO> pg = new Page<>(page, pageSize);

        IPage<CollectItemVO> result = collectionMapper.selectCollectPage(pg, userId, null, null, null);

        Page<CollectItemVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        voPage.setRecords(result.getRecords());

        return voPage;

    }



    @Override

    public Page<AdminUserActionVO> listActions(Long userId, String actionType, int current, int size) {

        int page = Math.max(current, 1);

        int pageSize = Math.min(Math.max(size, 1), 50);

        LambdaQueryWrapper<UserResourceAction> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(UserResourceAction::getUserId, userId);

        if (StringUtils.hasText(actionType)) {

            wrapper.eq(UserResourceAction::getActionType, actionType.trim());

        }

        wrapper.orderByDesc(UserResourceAction::getCreateTime);

        Page<UserResourceAction> pg = userResourceActionMapper.selectPage(new Page<>(page, pageSize), wrapper);

        Page<AdminUserActionVO> voPage = new Page<>(pg.getCurrent(), pg.getSize(), pg.getTotal());

        voPage.setRecords(pg.getRecords().stream().map(this::toActionVo).collect(Collectors.toList()));

        return voPage;

    }



    @Override

    public Page<AdminUserLoginLogVO> listLoginLogs(Long userId, int current, int size) {

        int page = Math.max(current, 1);

        int pageSize = Math.min(Math.max(size, 1), 50);

        long total = userLoginLogReadMapper.countByUser(userId);

        int offset = (page - 1) * pageSize;

        List<AdminUserLoginLogVO> records = userLoginLogReadMapper.selectPageByUser(userId, offset, pageSize);

        Page<AdminUserLoginLogVO> voPage = new Page<>(page, pageSize, total);

        voPage.setRecords(records);

        return voPage;

    }



    private AdminUserUploadVO toUploadVo(PrimaryChineseResource r) {

        AdminUserUploadVO vo = new AdminUserUploadVO();

        vo.setId(r.getId());

        vo.setTitle(r.getTitle());

        vo.setStage(r.getStage());

        vo.setSubject(r.getSubject());

        vo.setModule(r.getModule());

        vo.setType(r.getType());

        vo.setStatus(r.getStatus());

        vo.setStatusLabel(ResourceStatusConstants.auditStatusLabel(r.getAuditStatus(), r.getStatus())
                + " / " + ResourceStatusConstants.publishStatusLabel(r.getPublishStatus(), r.getStatus()));

        vo.setUploadTime(r.getUploadTime());

        return vo;

    }



    private AdminUserActionVO toActionVo(UserResourceAction row) {

        AdminUserActionVO vo = new AdminUserActionVO();

        vo.setId(row.getId());

        vo.setActionType(row.getActionType());

        vo.setResourceId(row.getResourceId());

        vo.setResourceType(row.getResourceType());

        vo.setTitle(row.getTitle());

        vo.setKeyword(row.getKeyword());

        vo.setHitCount(row.getHitCount());

        vo.setIp(row.getIp());

        vo.setSourceApi(row.getSourceApi());

        vo.setCreateTime(row.getCreateTime());

        return vo;

    }

}


