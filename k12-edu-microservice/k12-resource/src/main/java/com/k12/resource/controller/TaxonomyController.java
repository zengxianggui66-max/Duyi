package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.TaxonomyEditionVO;
import com.k12.common.dto.TaxonomyGradeVO;
import com.k12.common.dto.TaxonomyModuleVO;
import com.k12.common.dto.TaxonomyResourceTypeVO;
import com.k12.common.dto.TaxonomyStageVO;
import com.k12.common.dto.TaxonomySubjectVO;
import com.k12.common.dto.TaxonomyVolumeVO;
import com.k12.resource.service.TaxonomyReadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Phase 5-A：分类维度统一读 API（C 端 / 上传 / 浏览共用）
 */
@RestController
@RequestMapping("/api/taxonomy")
public class TaxonomyController {

    private final TaxonomyReadService taxonomyReadService;
    public TaxonomyController(TaxonomyReadService taxonomyReadService) {
        this.taxonomyReadService = taxonomyReadService;
    }


    /**
     * 学段列表
     * GET /api/taxonomy/stages
     */
    @GetMapping("/stages")
    public Result<List<TaxonomyStageVO>> listStages(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listStages(includeDisabled));
    }

    /**
     * 学科列表（按学段过滤）
     * GET /api/taxonomy/subjects?stage=primary
     * stage 支持 code（primary）或中文名（小学）；不传则返回全部启用学科
     */
    @GetMapping("/subjects")
    public Result<List<TaxonomySubjectVO>> listSubjects(
            @RequestParam(required = false) String stage,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listSubjects(stage, includeDisabled));
    }

    /**
     * 年级列表（按学段过滤）
     * GET /api/taxonomy/grades?stage=primary
     */
    @GetMapping("/grades")
    public Result<List<TaxonomyGradeVO>> listGrades(
            @RequestParam String stage,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listGrades(stage, includeDisabled));
    }

    /**
     * 教材版本列表（按学段 + 学科）
     * GET /api/taxonomy/editions?stage=primary&subject=chinese
     */
    @GetMapping("/editions")
    public Result<List<TaxonomyEditionVO>> listEditions(
            @RequestParam(required = false) String stage,
            @RequestParam String subject,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listEditions(stage, subject, includeDisabled));
    }

    /**
     * 教材册别列表（按学段）
     * GET /api/taxonomy/volumes?stage=primary
     */
    @GetMapping("/volumes")
    public Result<List<TaxonomyVolumeVO>> listVolumes(
            @RequestParam String stage,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listVolumes(stage, includeDisabled));
    }

    /**
     * 栏目列表（按学段）
     * GET /api/taxonomy/modules?stage=primary
     */
    @GetMapping("/modules")
    public Result<List<TaxonomyModuleVO>> listModules(
            @RequestParam String stage,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listModules(stage, includeDisabled));
    }

    /**
     * 资源类型列表（叶子节点）
     * GET /api/taxonomy/resource-types?stage=primary&subject=chinese&module=同步备课
     */
    @GetMapping("/resource-types")
    public Result<List<TaxonomyResourceTypeVO>> listResourceTypes(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(taxonomyReadService.listResourceTypes(stage, subject, module, includeDisabled));
    }
}
