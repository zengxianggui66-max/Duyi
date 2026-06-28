package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.DictionaryItemVO;
import com.k12.resource.service.DictionaryReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 5-D：字典/标签统一读 API（C 端 / 浏览 / 上传共用）
 */
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    private final DictionaryReadService dictionaryReadService;
    public DictionaryController(DictionaryReadService dictionaryReadService) {
        this.dictionaryReadService = dictionaryReadService;
    }


    @GetMapping("/exam-scenes")
    public Result<List<DictionaryItemVO>> listExamScenes(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryReadService.listExamScenes(includeDisabled));
    }

    @GetMapping("/teaching-scenes")
    public Result<List<DictionaryItemVO>> listTeachingScenes(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryReadService.listTeachingScenes(includeDisabled));
    }

    @GetMapping("/file-formats")
    public Result<List<DictionaryItemVO>> listFileFormats(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryReadService.listFileFormats(includeDisabled));
    }

    @GetMapping("/regions")
    public Result<List<DictionaryItemVO>> listRegions(
            @RequestParam(required = false) Integer parentId,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryReadService.listRegions(parentId, includeDisabled));
    }

    @GetMapping("/browse-tags")
    public Result<List<DictionaryItemVO>> listBrowseTags(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(dictionaryReadService.listBrowseTags(stage, module, includeDisabled));
    }
}
