package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.*;
import com.k12.resource.cache.HomeCacheService;
import com.k12.resource.service.HomeLatestColumnService;
import com.k12.resource.service.HomeOpsService;
import com.k12.resource.service.HomeFuncChannelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 7-A: home ops read APIs (banner / quick entry / hot word).
 */
@RestController
@RequestMapping("/api/home")
public class HomeOpsController {

    private final HomeOpsService homeOpsService;
    private final HomeLatestColumnService homeLatestColumnService;
    private final HomeFuncChannelService homeFuncChannelService;
    private final HomeCacheService homeCacheService;
    public HomeOpsController(HomeOpsService homeOpsService, HomeLatestColumnService homeLatestColumnService, HomeFuncChannelService homeFuncChannelService, HomeCacheService homeCacheService) {
        this.homeOpsService = homeOpsService;
        this.homeLatestColumnService = homeLatestColumnService;
        this.homeFuncChannelService = homeFuncChannelService;
        this.homeCacheService = homeCacheService;
    }


    @GetMapping("/banners")
    public Result<List<HomeBannerVO>> banners(
            @RequestParam(defaultValue = "home_hero") String slotCode,
            @RequestParam(required = false) String stage) {
        String key = "home:banners:" + slotCode + ":" + (stage == null ? "" : stage);
        return Result.success(homeCacheService.getOrLoad(key,
                () -> homeOpsService.listBanners(slotCode, stage, true)));
    }

    @GetMapping("/quick-entries")
    public Result<List<HomeQuickEntryVO>> quickEntries(
            @RequestParam(required = false) String stage) {
        String key = "home:quick:" + (stage == null ? "" : stage);
        return Result.success(homeCacheService.getOrLoad(key,
                () -> homeOpsService.listQuickEntries(stage, true)));
    }

    @GetMapping("/hot-words")
    public Result<List<HomeHotWordVO>> hotWords(
            @RequestParam(required = false) String stage) {
        String key = "home:hot:" + (stage == null ? "" : stage);
        return Result.success(homeCacheService.getOrLoad(key,
                () -> homeOpsService.listHotWords(stage, true)));
    }

    @GetMapping("/hero")
    public Result<HomeHeroVO> hero(
            @RequestParam(defaultValue = "home_hero") String slotCode,
            @RequestParam(required = false) String stage) {
        String key = "home:hero:" + slotCode + ":" + (stage == null ? "" : stage);
        return Result.success(homeCacheService.getOrLoad(key,
                () -> homeOpsService.getHero(slotCode, stage)));
    }

    @GetMapping("/latest-columns")
    public Result<List<HomeLatestColumnWithItemsVO>> latestColumns(
            @RequestParam(required = false) String stageKey) {
        String key = "home:latest:" + (stageKey == null ? "" : stageKey);
        return Result.success(homeCacheService.getOrLoad(key,
                () -> homeLatestColumnService.listPublicColumns(stageKey)));
    }

    @GetMapping("/bootstrap")
    public Result<HomePreviewVO> bootstrap(
            @RequestParam(required = false) String stage) {
        String stageKey = stage == null ? "primary" : stage;
        String key = "home:bootstrap:" + stageKey;
        return Result.success(homeCacheService.getOrLoad(key, () -> {
            HomePreviewVO vo = new HomePreviewVO();
            vo.setHero(homeOpsService.getHero("home_hero", stageKey));
            vo.setLatestColumns(homeLatestColumnService.listPublicColumns(stageKey));
            vo.setFuncChannels(homeFuncChannelService.listFuncChannels());
            return vo;
        }));
    }
}
