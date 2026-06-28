package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.HomePanelListVO;
import com.k12.resource.service.HomePanelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 首页三大专区数据接口
 * GET /api/home/panels/sync-prep
 * GET /api/home/panels/paper-zone
 * GET /api/home/panels/promotion
 */
@Slf4j
@RestController
@RequestMapping("/api/home/panels")
public class HomePanelController {

    private final HomePanelService homePanelService;
    public HomePanelController(HomePanelService homePanelService) {
        this.homePanelService = homePanelService;
    }


    @GetMapping("/sync-prep")
    public Result<HomePanelListVO> syncPrep(
            @RequestParam String stageKey,
            @RequestParam(required = false) String subjectName,
            @RequestParam String tabKey,
            @RequestParam(required = false) Integer limit) {
        try {
            return Result.success(homePanelService.listSyncPrep(stageKey, subjectName, tabKey, limit));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询同步备课首页列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/paper-zone")
    public Result<HomePanelListVO> paperZone(
            @RequestParam String stageKey,
            @RequestParam(required = false) String gradeName,
            @RequestParam String tabKey,
            @RequestParam(required = false) Integer limit) {
        try {
            return Result.success(homePanelService.listPaperZone(stageKey, gradeName, tabKey, limit));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询试卷专区首页列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/promotion")
    public Result<HomePanelListVO> promotion(
            @RequestParam String examType,
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) Integer limit) {
        try {
            return Result.success(homePanelService.listPromotion(examType, topic, limit));
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询升学专区首页列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
