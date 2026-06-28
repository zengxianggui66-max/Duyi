package com.k12.resource.controller;

import com.k12.common.BusinessException;
import com.k12.common.Result;
import com.k12.common.dto.MyUploadStatsVO;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.dto.PrimaryChineseResourceVO;
import com.k12.common.dto.ResourceRejectInfoVO;
import com.k12.common.dto.ResourceSuiteVO;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.security.UserIdResolver;
import com.k12.resource.service.PrimaryChineseResourceService;
import com.k12.resource.service.UploadPlacementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小学语文学科资源Controller
 * 基础路径：/api/primary-chinese
 */
@Slf4j
@RestController
@RequestMapping("/api/primary-chinese")
public class PrimaryChineseResourceController {

    private final PrimaryChineseResourceService primaryChineseResourceService;
    private final UploadPlacementService uploadPlacementService;
    private final UserIdResolver userIdResolver;
    public PrimaryChineseResourceController(PrimaryChineseResourceService primaryChineseResourceService, UploadPlacementService uploadPlacementService, UserIdResolver userIdResolver) {
        this.primaryChineseResourceService = primaryChineseResourceService;
        this.uploadPlacementService = uploadPlacementService;
        this.userIdResolver = userIdResolver;
    }


    // =================== 查询接口 ===================

    @GetMapping("/page")
    public Result<Map<String, Object>> listByPage(PrimaryChineseResourceQueryDTO dto) {
        try {
            Map<String, Object> result = primaryChineseResourceService.listByPage(dto);
            @SuppressWarnings("unchecked")
            List<PrimaryChineseResource> records = (List<PrimaryChineseResource>) result.get("records");
            if (records != null) {
                result.put("records", toVoList(records));
            }
            return Result.success(result);
        } catch (Exception e) {
            log.error("分页查询小学语文资源失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/mine/stats")
    public Result<MyUploadStatsVO> myUploadStats(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(primaryChineseResourceService.getMyUploadStats(userId));
    }

    /** 上传者查看最新驳回信息（个人中心「未通过」展示） */
    @GetMapping("/mine/reject-info/{id}")
    public Result<ResourceRejectInfoVO> getMyRejectInfo(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(primaryChineseResourceService.getMyResourceRejectInfo(id, userId));
    }

    @GetMapping("/list")
    public Result<List<PrimaryChineseResourceVO>> listAll(PrimaryChineseResourceQueryDTO dto) {
        try {
            List<PrimaryChineseResource> list = primaryChineseResourceService.listAll(dto);
            return Result.success(toVoList(list));
        } catch (Exception e) {
            log.error("查询小学语文资源列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public Result<PrimaryChineseResourceVO> getById(@PathVariable Long id) {
        PrimaryChineseResource resource = primaryChineseResourceService.getPublicById(id);
        if (resource == null) {
            return Result.error("资源不存在或未上架");
        }
        return Result.success(PrimaryChineseResourceVO.fromEntity(resource));
    }

    // =================== 筛选枚举接口 ===================

    @GetMapping("/filter-options")
    public Result<Map<String, Object>> getFilterOptions() {
        try {
            return Result.success(primaryChineseResourceService.getFilterOptions());
        } catch (Exception e) {
            log.error("获取筛选枚举失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 上传位置联动筛选项（按学段/学科过滤册别与版本）
     */
    @GetMapping("/upload-filter-options")
    public Result<Map<String, Object>> getUploadFilterOptions(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String module) {
        try {
            return Result.success(uploadPlacementService.getUploadFilterOptions(stage, subject, module));
        } catch (Exception e) {
            log.error("获取上传筛选项失败", e);
            return Result.error("获取上传筛选项失败，请稍后重试");
        }
    }

    @GetMapping("/grade-names")
    public Result<List<String>> getGradeNames() {
        return Result.success(primaryChineseResourceService.getGradeNames());
    }

    @GetMapping("/editions")
    public Result<List<String>> getEditions() {
        return Result.success(primaryChineseResourceService.getEditions());
    }

    @GetMapping("/modules")
    public Result<List<String>> getModules() {
        return Result.success(primaryChineseResourceService.getModules());
    }

    @GetMapping("/types")
    public Result<List<String>> getTypes() {
        return Result.success(primaryChineseResourceService.getTypes());
    }

    @GetMapping("/unit-names")
    public Result<List<String>> getUnitNames(
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String edition) {
        try {
            return Result.success(primaryChineseResourceService.getUnitNames(gradeName, edition));
        } catch (Exception e) {
            log.error("获取单元列表失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 单元树（单元 + 课文 subUnits）
     * GET /api/primary-chinese/unit-tree?volumeKey=y1s2&gradeName=一年级下册&edition=统编版&subject=语文
     */
    /**
     * 成套资源（按类型聚合）
     */
    @GetMapping("/suites")
    public Result<List<ResourceSuiteVO>> listSuites(PrimaryChineseResourceQueryDTO dto) {
        try {
            return Result.success(primaryChineseResourceService.listSuites(dto));
        } catch (Exception e) {
            log.error("查询成套资源失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }

    /**
     * 各栏目资源数量统计（与 edu-resource/module-stats 对齐）
     */
    @GetMapping("/module-stats")
    public Result<List<Map<String, Object>>> getModuleStats(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String edition) {
        try {
            return Result.success(primaryChineseResourceService.countByModule(
                    stage, subject, gradeName, edition));
        } catch (Exception e) {
            log.error("获取栏目统计失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    @GetMapping("/unit-tree")
    public Result<List<UnitTreeNodeVO>> getUnitTree(
            @RequestParam(required = false) String volumeKey,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String edition,
            @RequestParam(required = false) String subject) {
        try {
            return Result.success(primaryChineseResourceService.getUnitTree(
                    volumeKey, gradeName, edition, subject));
        } catch (Exception e) {
            log.error("获取单元树失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // =================== 草稿接口 ===================

    @GetMapping("/drafts")
    public Result<List<PrimaryChineseResourceVO>> listDrafts(HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        return Result.success(toVoList(primaryChineseResourceService.listDrafts(userId)));
    }

    @GetMapping("/draft/{id}")
    public Result<PrimaryChineseResourceVO> getDraft(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        PrimaryChineseResource draft = primaryChineseResourceService.getDraft(id, userId);
        if (draft == null) {
            return Result.error("草稿不存在");
        }
        return Result.success(PrimaryChineseResourceVO.fromEntity(draft));
    }

    @PostMapping("/draft/save")
    public Result<PrimaryChineseResourceVO> saveDraft(
            @RequestBody PrimaryChineseResourceVO vo,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            if (userId == null) {
                return Result.fail("请先登录");
            }
            PrimaryChineseResource saved = primaryChineseResourceService.saveDraft(vo.toEntity(), userId);
            return Result.success(PrimaryChineseResourceVO.fromEntity(saved));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @PostMapping("/draft/{id}/submit")
    public Result<PrimaryChineseResourceVO> submitDraft(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            if (userId == null) {
                return Result.fail("请先登录");
            }
            PrimaryChineseResource submitted = primaryChineseResourceService.submitDraft(id, userId);
            return Result.success(PrimaryChineseResourceVO.fromEntity(submitted));
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交草稿失败, id={}", id, e);
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    /** 保存并提交审核（聚合：saveDraft + submitDraft） */
    @PostMapping("/draft/submit")
    public Result<PrimaryChineseResourceVO> submitDraftWithPayload(
            @RequestBody PrimaryChineseResourceVO vo,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            if (userId == null) {
                return Result.fail("请先登录");
            }
            PrimaryChineseResource submitted = primaryChineseResourceService.submitFromPayload(
                    vo.toEntity(), userId);
            return Result.success(PrimaryChineseResourceVO.fromEntity(submitted));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("提交审核失败", e);
            return Result.error("提交失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/draft/{id}")
    public Result<Void> deleteDraft(
            @PathVariable Long id,
            HttpServletRequest request) {
        try {
            Long userId = userIdResolver.resolve(request);
            primaryChineseResourceService.deleteDraft(id, userId);
            return Result.success(null);
        } catch (IllegalStateException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("删除草稿失败, id={}", id, e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }

    /** 未通过资源复制为新草稿，供重新上传 */
    @PostMapping("/rejected/{id}/clone-draft")
    public Result<PrimaryChineseResourceVO> cloneRejectedToDraft(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        try {
            PrimaryChineseResource draft = primaryChineseResourceService.cloneRejectedToDraft(id, userId);
            return Result.success(PrimaryChineseResourceVO.fromEntity(draft));
        } catch (BusinessException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            log.error("复制未通过资源为草稿失败, id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    /** 撤回待审核资源（status: 0 → -1 草稿） */
    @PostMapping("/pending/{id}/withdraw")
    public Result<PrimaryChineseResourceVO> withdrawPending(
            @PathVariable Long id,
            HttpServletRequest request) {
        Long userId = userIdResolver.resolve(request);
        if (userId == null) {
            return Result.fail("请先登录");
        }
        try {
            PrimaryChineseResource resource = primaryChineseResourceService.withdrawPending(id, userId);
            return Result.success(PrimaryChineseResourceVO.fromEntity(resource));
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("撤回待审核资源失败, id={}", id, e);
            return Result.error("操作失败：" + e.getMessage());
        }
    }

    // =================== 写操作接口 ===================

    @PostMapping("/save")
    public Result<PrimaryChineseResourceVO> save(
            @RequestBody PrimaryChineseResourceVO vo,
            HttpServletRequest request) {
        try {
            PrimaryChineseResource entity = vo.toEntity();
            Long userId = userIdResolver.resolve(request);
            if (userId != null && entity.getUploaderId() == null) {
                entity.setUploaderId(userId);
            }
            PrimaryChineseResource saved = primaryChineseResourceService.save(entity);
            return Result.success(PrimaryChineseResourceVO.fromEntity(saved));
        } catch (BusinessException e) {
            return Result.fail(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存小学语文资源失败", e);
            return Result.error("保存失败：" + e.getMessage());
        }
    }

    @PostMapping("/batch-save")
    public Result<Map<String, Object>> batchSave(
            @RequestBody List<PrimaryChineseResourceVO> resources,
            HttpServletRequest request) {
        try {
            if (resources == null || resources.isEmpty()) {
                return Result.fail("请提供待保存的资源列表");
            }
            Long userId = userIdResolver.resolve(request);
            int successCount = 0;
            List<String> errors = new java.util.ArrayList<>();
            for (PrimaryChineseResourceVO vo : resources) {
                try {
                    PrimaryChineseResource entity = vo.toEntity();
                    if (userId != null && entity.getUploaderId() == null) {
                        entity.setUploaderId(userId);
                    }
                    primaryChineseResourceService.saveLegacyWrite(entity);
                    successCount++;
                } catch (Exception e) {
                    errors.add("title=" + vo.getTitle() + " 错误：" + e.getMessage());
                }
            }
            Map<String, Object> result = new HashMap<>();
            result.put("successCount", successCount);
            result.put("totalCount", resources.size());
            result.put("errors", errors);
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量保存小学语文资源失败", e);
            return Result.error("批量保存失败：" + e.getMessage());
        }
    }

    /**
     * 文件流代理（预览/下载 OSS 资源，解决浏览器 CORS 与私有桶访问问题）
     * GET /api/primary-chinese/{id}/file?disposition=inline|attachment
     */
    @GetMapping("/{id}/file")
    public void streamFile(
            @PathVariable Long id,
            @RequestParam(defaultValue = "inline") String disposition,
            HttpServletRequest request,
            HttpServletResponse response) {
        primaryChineseResourceService.streamFile(id, disposition, request, response);
    }

    @PostMapping("/{id}/download")
    public Result<String> incrementDownload(@PathVariable Long id) {
        try {
            primaryChineseResourceService.incrementDownloadCount(id);
            return Result.success("下载计数已更新");
        } catch (Exception e) {
            log.error("更新下载次数失败, id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    @PostMapping("/{id}/view")
    public Result<String> incrementView(@PathVariable Long id) {
        try {
            primaryChineseResourceService.incrementViewCount(id);
            return Result.success("浏览计数已更新");
        } catch (Exception e) {
            log.error("更新浏览次数失败, id={}", id, e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }

    private List<PrimaryChineseResourceVO> toVoList(List<PrimaryChineseResource> list) {
        return list.stream()
                .map(PrimaryChineseResourceVO::fromEntity)
                .collect(Collectors.toList());
    }
}
