package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.HomeFuncChannelsVO;
import com.k12.resource.cache.HomeCacheService;
import com.k12.resource.service.HomeFuncChannelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页顶栏功能入口元数据
 * GET /api/home/func-channels
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
public class HomeFuncChannelController {

    private final HomeFuncChannelService homeFuncChannelService;
    private final HomeCacheService homeCacheService;
    public HomeFuncChannelController(HomeFuncChannelService homeFuncChannelService, HomeCacheService homeCacheService) {
        this.homeFuncChannelService = homeFuncChannelService;
        this.homeCacheService = homeCacheService;
    }


    @GetMapping("/func-channels")
    public Result<HomeFuncChannelsVO> funcChannels() {
        try {
            return Result.success(homeCacheService.getOrLoad("home:func-channels",
                    homeFuncChannelService::listFuncChannels));
        } catch (Exception e) {
            log.error("查询首页功能入口配置失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
