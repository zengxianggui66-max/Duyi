package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.BrowseStatsResultVO;
import com.k12.common.dto.BrowseModuleStatVO;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.dto.PrimaryChineseResourceVO;
import com.k12.common.dto.ResourceSuiteVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.service.ResourceBrowseService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统一资源浏览 API（M3）
 */
@RestController
@RequestMapping("/api/resources/browse")
public class ResourceBrowseController {

    private final ResourceBrowseService resourceBrowseService;
    public ResourceBrowseController(ResourceBrowseService resourceBrowseService) {
        this.resourceBrowseService = resourceBrowseService;
    }


    @GetMapping
    public Result<Map<String, Object>> browsePage(PrimaryChineseResourceQueryDTO dto) {
        Map<String, Object> result = resourceBrowseService.listByPage(dto);
        @SuppressWarnings("unchecked")
        List<PrimaryChineseResource> records = (List<PrimaryChineseResource>) result.get("records");
        if (records != null) {
            result.put("records", records.stream()
                    .map(PrimaryChineseResourceVO::fromEntity)
                    .collect(Collectors.toList()));
        }
        return Result.success(result);
    }

    @GetMapping("/stats")
    public Result<BrowseStatsResultVO> browseStats(PrimaryChineseResourceQueryDTO dto) {
        return Result.success(resourceBrowseService.stats(dto));
    }

    @GetMapping("/module-stats")
    public Result<List<BrowseModuleStatVO>> browseModuleStats(PrimaryChineseResourceQueryDTO dto) {
        return Result.success(resourceBrowseService.moduleStats(dto));
    }

    @GetMapping("/suites")
    public Result<List<ResourceSuiteVO>> browseSuites(PrimaryChineseResourceQueryDTO dto) {
        return Result.success(resourceBrowseService.listSuites(dto));
    }
}
