package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.OpsChannelBootstrapVO;
import com.k12.resource.cache.HomeCacheService;
import com.k12.resource.service.OpsChannelService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channel")
public class OpsChannelController {

    private final OpsChannelService opsChannelService;
    private final HomeCacheService homeCacheService;
    public OpsChannelController(OpsChannelService opsChannelService, HomeCacheService homeCacheService) {
        this.opsChannelService = opsChannelService;
        this.homeCacheService = homeCacheService;
    }


    @GetMapping("/{code}")
    public Result<OpsChannelBootstrapVO> bootstrap(@PathVariable String code) {
        String key = "home:channel:" + code;
        return Result.success(homeCacheService.getOrLoad(key, () -> opsChannelService.getBootstrap(code)));
    }
}
