package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.CompetitionQueryDTO;
import com.k12.common.dto.CompetitionResourceUploadDTO;
import com.k12.common.entity.CompetitionResource;
import com.k12.resource.service.CompetitionZoneService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 竞赛专区 API
 * 基础路径：/api/competition
 */
@RestController
@RequestMapping("/api/competition")
public class CompetitionController {

    private final CompetitionZoneService competitionZoneService;
    public CompetitionController(CompetitionZoneService competitionZoneService) {
        this.competitionZoneService = competitionZoneService;
    }


    @GetMapping("/filter-options")
    public Result<Map<String, Object>> filterOptions() {
        return Result.success(competitionZoneService.getFilterOptions());
    }

    @GetMapping("/resources/page")
    public Result<Map<String, Object>> listResources(CompetitionQueryDTO dto) {
        return Result.success(competitionZoneService.listResourcesByPage(dto));
    }

    @GetMapping("/resources/{id}")
    public Result<CompetitionResource> getResource(@PathVariable Long id) {
        return Result.success(competitionZoneService.getResource(id));
    }

    @PostMapping("/resources/{id}/view")
    public Result<Void> viewResource(@PathVariable Long id) {
        competitionZoneService.incrementResourceView(id);
        return Result.success();
    }

    @PostMapping("/resources/{id}/download")
    public Result<Void> downloadResource(@PathVariable Long id) {
        competitionZoneService.incrementResourceDownload(id);
        return Result.success();
    }

    @GetMapping("/packages/page")
    public Result<Map<String, Object>> listPackages(CompetitionQueryDTO dto) {
        return Result.success(competitionZoneService.listPackagesByPage(dto));
    }

    @GetMapping("/packages/{id}")
    public Result<Map<String, Object>> getPackage(@PathVariable Long id) {
        return Result.success(competitionZoneService.getPackageDetail(id));
    }

    @PostMapping("/packages/{id}/download")
    public Result<Void> downloadPackage(@PathVariable Long id) {
        competitionZoneService.incrementPackageDownload(id);
        return Result.success();
    }

    @PostMapping(value = "/resources/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<CompetitionResource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String summary,
            @RequestParam String category,
            @RequestParam(defaultValue = "all") String gradeStage,
            @RequestParam(required = false) String subject,
            @RequestParam String resourceForm,
            @RequestParam(required = false) String competitionName,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String icon,
            @RequestParam(defaultValue = "1") Integer isFree) {
        CompetitionResourceUploadDTO dto = new CompetitionResourceUploadDTO();
        dto.setTitle(title);
        dto.setSummary(summary);
        dto.setCategory(category);
        dto.setGradeStage(gradeStage);
        dto.setSubject(subject);
        dto.setResourceForm(resourceForm);
        dto.setCompetitionName(competitionName);
        dto.setTags(tags);
        dto.setIcon(icon);
        dto.setIsFree(isFree);
        return Result.success(competitionZoneService.createResource(file, dto));
    }
}
