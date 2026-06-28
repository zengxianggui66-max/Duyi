package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.TopicQueryDTO;
import com.k12.common.dto.TopicResourceUploadDTO;
import com.k12.common.entity.TopicResource;
import com.k12.resource.service.TopicZoneService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 专题资源专区 API
 * 基础路径：/api/topic
 */
@RestController
@RequestMapping("/api/topic")
public class TopicController {

    private final TopicZoneService topicZoneService;
    public TopicController(TopicZoneService topicZoneService) {
        this.topicZoneService = topicZoneService;
    }


    @GetMapping("/filter-options")
    public Result<Map<String, Object>> filterOptions() {
        return Result.success(topicZoneService.getFilterOptions());
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> stats() {
        return Result.success(topicZoneService.getZoneStats());
    }

    @GetMapping("/calendar-hint")
    public Result<Map<String, Object>> calendarHint() {
        return Result.success(topicZoneService.getCalendarHint());
    }

    @GetMapping("/hot-keywords")
    public Result<List<Map<String, Object>>> hotKeywords(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(topicZoneService.getHotKeywords(limit));
    }

    @GetMapping("/resources/hot")
    public Result<List<TopicResource>> hotResources(
            @RequestParam(defaultValue = "8") Integer limit,
            @RequestParam(required = false) String region) {
        return Result.success(topicZoneService.listHotResources(limit, region));
    }

    @GetMapping("/resources/latest")
    public Result<List<TopicResource>> latestResources(
            @RequestParam(defaultValue = "8") Integer limit,
            @RequestParam(required = false) String region) {
        return Result.success(topicZoneService.listLatestResources(limit, region));
    }

    @GetMapping("/resources/page")
    public Result<Map<String, Object>> listResources(TopicQueryDTO dto) {
        return Result.success(topicZoneService.listResourcesByPage(dto));
    }

    @GetMapping("/resources/{id}")
    public Result<TopicResource> getResource(@PathVariable Long id) {
        return Result.success(topicZoneService.getResource(id));
    }

    @PostMapping("/resources/{id}/view")
    public Result<Void> viewResource(@PathVariable Long id) {
        topicZoneService.incrementResourceView(id);
        return Result.success();
    }

    @PostMapping("/resources/{id}/download")
    public Result<Void> downloadResource(@PathVariable Long id) {
        topicZoneService.incrementResourceDownload(id);
        return Result.success();
    }

    @GetMapping("/albums/page")
    public Result<Map<String, Object>> listAlbums(TopicQueryDTO dto) {
        return Result.success(topicZoneService.listAlbumsByPage(dto));
    }

    @GetMapping("/albums/{id}")
    public Result<Map<String, Object>> getAlbum(@PathVariable Long id) {
        return Result.success(topicZoneService.getAlbumDetail(id));
    }

    @PostMapping("/albums/{id}/download")
    public Result<Void> downloadAlbum(@PathVariable Long id) {
        topicZoneService.incrementAlbumDownload(id);
        return Result.success();
    }

    @PostMapping(value = "/resources/upload", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TopicResource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestParam String title,
            @RequestParam(required = false) String summary,
            @RequestParam String category,
            @RequestParam(defaultValue = "all") String region,
            @RequestParam(defaultValue = "all") String gradeStage,
            @RequestParam(required = false) String subject,
            @RequestParam String resourceForm,
            @RequestParam(required = false) String topicLabel,
            @RequestParam(required = false) String schoolYear,
            @RequestParam(required = false) String tags,
            @RequestParam(required = false) String icon,
            @RequestParam(defaultValue = "1") Integer isFree) {
        TopicResourceUploadDTO dto = new TopicResourceUploadDTO();
        dto.setTitle(title);
        dto.setSummary(summary);
        dto.setCategory(category);
        dto.setRegion(region);
        dto.setGradeStage(gradeStage);
        dto.setSubject(subject);
        dto.setResourceForm(resourceForm);
        dto.setTopicLabel(topicLabel);
        dto.setSchoolYear(schoolYear);
        dto.setTags(tags);
        dto.setIcon(icon);
        dto.setIsFree(isFree);
        return Result.success(topicZoneService.createResource(file, dto));
    }
}
