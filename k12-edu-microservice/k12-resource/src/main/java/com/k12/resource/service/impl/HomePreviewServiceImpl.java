package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.dto.HomePreviewVO;
import com.k12.common.entity.OpsChannel;
import com.k12.resource.mapper.OpsChannelMapper;
import com.k12.resource.service.*;
import org.springframework.stereotype.Service;

@Service
@SuppressWarnings("null")
public class HomePreviewServiceImpl implements HomePreviewService {

    private final HomeOpsService homeOpsService;
    private final HomeLatestColumnService homeLatestColumnService;
    private final HomeFuncChannelService homeFuncChannelService;
    private final OpsChannelMapper opsChannelMapper;
    public HomePreviewServiceImpl(HomeOpsService homeOpsService, HomeLatestColumnService homeLatestColumnService, HomeFuncChannelService homeFuncChannelService, OpsChannelMapper opsChannelMapper) {
        this.homeOpsService = homeOpsService;
        this.homeLatestColumnService = homeLatestColumnService;
        this.homeFuncChannelService = homeFuncChannelService;
        this.opsChannelMapper = opsChannelMapper;
    }


    @Override
    public HomePreviewVO buildPreview(String stageKey) {
        HomePreviewVO vo = new HomePreviewVO();
        vo.setHero(homeOpsService.getHero("home_hero", stageKey));
        vo.setLatestColumns(homeLatestColumnService.listPublicColumns(stageKey));
        vo.setFuncChannels(homeFuncChannelService.listFuncChannels());
        Long count = opsChannelMapper.selectCount(new LambdaQueryWrapper<OpsChannel>().eq(OpsChannel::getStatus, 1));
        vo.setOpsChannelCount(count != null ? count.intValue() : 0);
        return vo;
    }
}
