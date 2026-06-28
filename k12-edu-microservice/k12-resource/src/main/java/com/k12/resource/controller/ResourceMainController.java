package com.k12.resource.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.Result;
import com.k12.common.dto.BrowseModuleStatVO;
import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;
import com.k12.common.dto.ResourceSuiteVO;
import com.k12.common.entity.VAdminResourceMain;
import com.k12.resource.mapper.VAdminResourceMainMapper;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.ResourceBrowseService;
import com.k12.resource.service.ResourceMainService;
import com.k12.resource.service.UnifiedReadFeatureFlagService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Phase 3D: 前台统一资源主链接口
 */
@RestController
@RequestMapping("/api/resources")
public class ResourceMainController {

    private final ResourceMainService resourceMainService;
    private final ResourceBrowseService resourceBrowseService;
    private final UserIdResolver userIdResolver;
    private final UnifiedReadFeatureFlagService unifiedReadFeatureFlagService;
    private final VAdminResourceMainMapper vAdminResourceMainMapper;

    public ResourceMainController(ResourceMainService resourceMainService,
                                  ResourceBrowseService resourceBrowseService,
                                  UserIdResolver userIdResolver,
                                  UnifiedReadFeatureFlagService unifiedReadFeatureFlagService,
                                  VAdminResourceMainMapper vAdminResourceMainMapper) {
        this.resourceMainService = resourceMainService;
        this.resourceBrowseService = resourceBrowseService;
        this.userIdResolver = userIdResolver;
        this.unifiedReadFeatureFlagService = unifiedReadFeatureFlagService;
        this.vAdminResourceMainMapper = vAdminResourceMainMapper;
    }

    @GetMapping("/page")
    public Result<Page<ResourceMainVO>> page(ResourceMainQueryDTO query) {
        requireSourceEnabled(query != null ? query.getSourceType() : null);
        return Result.success(resourceMainService.page(query));
    }

    @GetMapping("/detail/{resourceId}")
    public Result<ResourceMainDetailVO> detail(@PathVariable Long resourceId) {
        ResourceMainDetailVO detail = resourceMainService.detail(resourceId);
        requireSourceEnabled(detail.getSourceType());
        return Result.success(detail);
    }

    @GetMapping("/resolve-global-id")
    public Result<Long> resolveGlobalId(
            @RequestParam String sourceType,
            @RequestParam Long sourceId) {
        requireSourceEnabled(sourceType);
        return Result.success(resourceMainService.resolveGlobalId(sourceType, sourceId));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats(ResourceMainQueryDTO query) {
        requireSourceEnabled(query != null ? query.getSourceType() : null);
        return Result.success(resourceMainService.stats(query));
    }

    @GetMapping("/types")
    public Result<List<Map<String, Object>>> types(ResourceMainQueryDTO query) {
        requireSourceEnabled(query != null ? query.getSourceType() : null);
        return Result.success(resourceMainService.types(query));
    }

    /** Phase 3I-B: 学科页成套资源（统一读，替代 /resources/browse/suites） */
    @GetMapping("/suites")
    public Result<List<ResourceSuiteVO>> suites(
            @RequestParam(defaultValue = "primary_chinese") String sourceType,
            PrimaryChineseResourceQueryDTO query) {
        requirePrimaryChineseSource(sourceType);
        return Result.success(resourceBrowseService.listSuites(query));
    }

    /** Phase 3I-B: 学科页栏目统计（统一读，替代 /resources/browse/module-stats） */
    @GetMapping("/module-stats")
    public Result<List<BrowseModuleStatVO>> moduleStats(
            @RequestParam(defaultValue = "primary_chinese") String sourceType,
            PrimaryChineseResourceQueryDTO query) {
        requirePrimaryChineseSource(sourceType);
        return Result.success(resourceBrowseService.moduleStats(query));
    }

    @GetMapping("/{resourceId}/preview")
    public Result<FilePreviewInfoDTO> preview(@PathVariable Long resourceId) {
        requireResourceEnabled(resourceId);
        return Result.success(resourceMainService.preview(resourceId));
    }

    @PostMapping("/{resourceId}/view")
    public Result<Void> view(@PathVariable Long resourceId, HttpServletRequest request) {
        requireResourceEnabled(resourceId);
        Long userId = userIdResolver.resolve(request);
        resourceMainService.view(resourceId, userId);
        return Result.success();
    }

    @PostMapping("/{resourceId}/download")
    public Result<Map<String, Object>> download(@PathVariable Long resourceId, HttpServletRequest request) {
        requireResourceEnabled(resourceId);
        Long userId = userIdResolver.resolve(request);
        return Result.success(resourceMainService.download(resourceId, userId));
    }

    @PostMapping("/{resourceId}/collect")
    public Result<Void> collect(@PathVariable Long resourceId, HttpServletRequest request) {
        requireResourceEnabled(resourceId);
        Long userId = userIdResolver.resolve(request);
        resourceMainService.collect(resourceId, userId);
        return Result.success();
    }

    private void requireResourceEnabled(Long resourceId) {
        VAdminResourceMain row = vAdminResourceMainMapper.findByGlobalId(resourceId);
        requireSourceEnabled(row != null ? row.getSourceType() : null);
    }

    private void requireSourceEnabled(String sourceType) {
        if (!unifiedReadFeatureFlagService.isSourceEnabled(sourceType)) {
            throw new com.k12.common.BusinessException(503, "统一资源读取未开启");
        }
    }

    private void requirePrimaryChineseSource(String sourceType) {
        if (!"primary_chinese".equals(sourceType)) {
            throw new com.k12.common.BusinessException(400, "暂仅支持 primary_chinese");
        }
        requireSourceEnabled(sourceType);
    }
}
