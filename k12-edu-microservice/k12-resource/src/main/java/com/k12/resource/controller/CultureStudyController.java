package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.CultureQueryDTO;
import com.k12.common.dto.CultureResourceUploadDTO;
import com.k12.common.entity.CultureResource;
import com.k12.resource.service.CultureStudyService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 传统文化 · 巴蜀研学 API
 * 基础路径：/api/culture
 */
@RestController
@RequestMapping("/api/culture")
public class CultureStudyController {

    private final CultureStudyService cultureStudyService;
    public CultureStudyController(CultureStudyService cultureStudyService) {
        this.cultureStudyService = cultureStudyService;
    }


    @GetMapping("/filter-options")
    public Result<Map<String, Object>> filterOptions() {
        return Result.success(cultureStudyService.getFilterOptions());
    }

    @GetMapping("/resources/page")
    public Result<Map<String, Object>> listResources(CultureQueryDTO dto) {
        return Result.success(cultureStudyService.listResourcesByPage(dto));
    }

    /**
     * 上传传统文化研学资源（需登录）
     */
    @PostMapping(value = "/resources/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CultureResource> createResource(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String summary,
            @RequestParam String category,
            @RequestParam(defaultValue = "bashu") String region,
            @RequestParam String durationType,
            @RequestParam(required = false) String durationLabel,
            @RequestParam(required = false) String suitableAudience,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "platform") String resourceKind,
            @RequestParam(required = false) String externalUrl,
            @RequestParam(required = false) String sourceName,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String icon,
            @RequestParam(defaultValue = "1") Integer isFree) {
        CultureResourceUploadDTO dto = new CultureResourceUploadDTO();
        dto.setTitle(title);
        dto.setSummary(summary);
        dto.setCategory(category);
        dto.setRegion(region);
        dto.setDurationType(durationType);
        dto.setDurationLabel(durationLabel);
        dto.setSuitableAudience(suitableAudience);
        dto.setLocation(location);
        dto.setResourceKind(resourceKind);
        dto.setExternalUrl(externalUrl);
        dto.setSourceName(sourceName);
        dto.setTags(tags);
        dto.setIcon(icon);
        dto.setIsFree(isFree);
        return Result.success(cultureStudyService.createResource(file, dto));
    }

    @GetMapping("/resources/{id}")
    public Result<CultureResource> getResource(@PathVariable Long id) {
        return Result.success(cultureStudyService.getResource(id));
    }

    @PostMapping("/resources/{id}/view")
    public Result<Void> viewResource(@PathVariable Long id) {
        cultureStudyService.incrementResourceView(id);
        return Result.success(null);
    }

    @PostMapping("/resources/{id}/download")
    public Result<Void> downloadResource(@PathVariable Long id) {
        cultureStudyService.incrementResourceDownload(id);
        return Result.success(null);
    }

    @GetMapping("/packages/page")
    public Result<Map<String, Object>> listPackages(CultureQueryDTO dto) {
        return Result.success(cultureStudyService.listPackagesByPage(dto));
    }

    @GetMapping("/packages/{id}")
    public Result<Map<String, Object>> getPackage(@PathVariable Long id) {
        return Result.success(cultureStudyService.getPackageDetail(id));
    }

    @PostMapping("/packages/{id}/download")
    public Result<Void> downloadPackage(@PathVariable Long id) {
        cultureStudyService.incrementPackageDownload(id);
        return Result.success(null);
    }
}
