package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.EduResourceQueryDTO;
import com.k12.common.entity.EduResource;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.resource.service.EduResourceService;
import com.k12.resource.service.UnitCatalogService;
import org.springframework.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * edu_resource 多维度资源 Controller
 * 基础路径：/api/edu-resource
 *
 * 接口列表：
 *   GET  /api/edu-resource/page          - 分页查询（主列表）
 *   GET  /api/edu-resource/list          - 不分页查询（成套/全量）
 *   GET  /api/edu-resource/{id}          - 资源详情
 *   POST /api/edu-resource/{id}/download - 增加下载计数
 *   POST /api/edu-resource/{id}/view     - 增加浏览计数
 *   GET  /api/edu-resource/filter-options - 筛选枚举（一次性获取全部）
 *   GET  /api/edu-resource/stages        - 学段列表
 *   GET  /api/edu-resource/subjects      - 学科列表
 *   GET  /api/edu-resource/editions      - 版本列表
 *   GET  /api/edu-resource/grades        - 年级列表
 *   GET  /api/edu-resource/modules       - 栏目列表
 *   GET  /api/edu-resource/resource-types - 资源类型列表
 *   GET  /api/edu-resource/units         - 单元列表（供侧边树）
 *   GET  /api/edu-resource/module-stats  - 各栏目资源数量统计
 */
@Slf4j
@RestController
@RequestMapping("/api/edu-resource")
public class EduResourceController {

    private final EduResourceService eduResourceService;
    private final UnitCatalogService unitCatalogService;
    public EduResourceController(EduResourceService eduResourceService, UnitCatalogService unitCatalogService) {
        this.eduResourceService = eduResourceService;
        this.unitCatalogService = unitCatalogService;
    }


    // =================== 查询接口 ===================

    /**
     * 分页查询资源列表（主列表）
     * GET /api/edu-resource/page
     * 参数：stageName, subjectName, editionName, gradeName, semesterName, volumeName,
     *       moduleName, resourceTypeName, unitName, keyword, fileExt,
     *       current, size, sortField, sortOrder
     */
    @GetMapping("/page")
    public Result<Map<String, Object>> listByPage(EduResourceQueryDTO dto) {
        try {
            return Result.success(eduResourceService.listByPage(dto));
        } catch (Exception e) {
            log.error("分页查询edu资源失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 不分页查询资源列表（成套资源/树形导航使用）
     * GET /api/edu-resource/list
     */
    @GetMapping("/list")
    public Result<List<EduResource>> listAll(EduResourceQueryDTO dto) {
        try {
            return Result.success(eduResourceService.listAll(dto));
        } catch (Exception e) {
            log.error("查询edu资源列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID查询资源详情
     * GET /api/edu-resource/{id}
     */
    @GetMapping("/{id}")
    public Result<EduResource> getById(@PathVariable Long id) {
        EduResource resource = eduResourceService.getById(id);
        if (resource == null) {
            return Result.error("资源不存在");
        }
        return Result.success(resource);
    }

    // =================== 统计/计数接口 ===================

    /**
     * 增加下载次数
     * POST /api/edu-resource/{id}/download
     */
    @PostMapping("/{id}/download")
    public Result<String> incrementDownload(@PathVariable Long id) {
        try {
            eduResourceService.incrementDownloadCount(id);
            return Result.success("下载计数已更新");
        } catch (Exception e) {
            log.error("更新下载次数失败, id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 增加浏览次数
     * POST /api/edu-resource/{id}/view
     */
    @PostMapping("/{id}/view")
    public Result<String> incrementView(@PathVariable Long id) {
        try {
            eduResourceService.incrementViewCount(id);
            return Result.success("浏览计数已更新");
        } catch (Exception e) {
            log.error("更新浏览次数失败, id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    /**
     * 增加收藏次数
     * POST /api/edu-resource/{id}/collect
     */
    @PostMapping("/{id}/collect")
    public Result<String> incrementCollect(@PathVariable Long id) {
        try {
            eduResourceService.incrementCollectCount(id);
            return Result.success("收藏计数已更新");
        } catch (Exception e) {
            log.error("更新收藏次数失败, id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    // =================== 筛选枚举接口 ===================

    /**
     * 一次性获取所有筛选枚举（减少请求次数）
     * GET /api/edu-resource/filter-options?stageName=小学&subjectName=语文
     * 返回：{ stages, subjects, editions, grades, modules, resourceTypes }
     */
    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getFilterOptions(
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String subjectName) {
        try {
            return Result.success(eduResourceService.getFilterOptions(stageName, subjectName));
        } catch (Exception e) {
            log.error("获取筛选枚举失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取学段列表（有数据的学段）
     * GET /api/edu-resource/stages
     */
    @GetMapping("/stages")
    public Result<List<Map<String, Object>>> getStages() {
        try {
            return Result.success(eduResourceService.getStages());
        } catch (Exception e) {
            log.error("获取学段列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取学科列表（按学段过滤）
     * GET /api/edu-resource/subjects?stageName=小学
     */
    @GetMapping("/subjects")
    public Result<List<Map<String, Object>>> getSubjects(
            @RequestParam(required = false) String stageName) {
        try {
            return Result.success(eduResourceService.getSubjects(stageName));
        } catch (Exception e) {
            log.error("获取学科列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取版本列表
     * GET /api/edu-resource/editions?stageName=小学&subjectName=语文
     */
    @GetMapping("/editions")
    public Result<List<Map<String, Object>>> getEditions(
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String subjectName) {
        try {
            return Result.success(eduResourceService.getEditions(stageName, subjectName));
        } catch (Exception e) {
            log.error("获取版本列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取年级列表
     * GET /api/edu-resource/grades?stageName=小学
     */
    @GetMapping("/grades")
    public Result<List<Map<String, Object>>> getGrades(
            @RequestParam(required = false) String stageName) {
        try {
            return Result.success(eduResourceService.getGrades(stageName));
        } catch (Exception e) {
            log.error("获取年级列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取栏目列表
     * GET /api/edu-resource/modules?stageName=小学&subjectName=语文
     */
    @GetMapping("/modules")
    public Result<List<Map<String, Object>>> getModules(
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String subjectName) {
        try {
            return Result.success(eduResourceService.getModules(stageName, subjectName));
        } catch (Exception e) {
            log.error("获取栏目列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取资源类型列表
     * GET /api/edu-resource/resource-types?stageName=小学
     */
    @GetMapping("/resource-types")
    public Result<List<Map<String, Object>>> getResourceTypes(
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String moduleName) {
        try {
            return Result.success(eduResourceService.getBrowseResourceTypes(stageName, subjectName, moduleName));
        } catch (Exception e) {
            log.error("获取资源类型列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取单元列表（按年级/版本/册别/学科过滤，供侧边目录树）
     * GET /api/edu-resource/units?gradeName=一年级&editionName=统编版&volumeName=上册&subjectName=语文
     */
    @GetMapping("/units")
    public Result<List<Map<String, Object>>> getUnits(
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String editionName,
            @RequestParam(required = false) String volumeName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String volumeKey) {
        try {
            String combinedGrade = gradeName;
            if (StringUtils.hasText(gradeName) && StringUtils.hasText(volumeName)
                    && !gradeName.contains(volumeName)) {
                combinedGrade = gradeName + volumeName;
            }
            List<UnitTreeNodeVO> tree = unitCatalogService.getUnitTree(
                    volumeKey, combinedGrade, editionName, subjectName);
            if (!tree.isEmpty()) {
                List<Map<String, Object>> result = new ArrayList<>();
                for (UnitTreeNodeVO node : tree) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("name", node.getName());
                    item.put("subUnits", node.getSubUnits());
                    result.add(item);
                }
                return Result.success(result);
            }
            return Result.success(eduResourceService.getUnits(gradeName, editionName, volumeName, subjectName));
        } catch (Exception e) {
            log.error("获取单元列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取各栏目资源数量统计
     * GET /api/edu-resource/module-stats?stageName=小学&subjectName=语文&gradeName=一年级&editionName=统编版
     */
    @GetMapping("/module-stats")
    public Result<List<Map<String, Object>>> getModuleStats(
            @RequestParam(required = false) String stageName,
            @RequestParam(required = false) String subjectName,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String editionName) {
        try {
            return Result.success(eduResourceService.countByModule(stageName, subjectName, gradeName, editionName));
        } catch (Exception e) {
            log.error("获取栏目统计失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }
}
