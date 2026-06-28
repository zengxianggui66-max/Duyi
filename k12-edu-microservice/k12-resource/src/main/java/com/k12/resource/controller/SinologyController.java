package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.SinologyUnitBundleVO;
import com.k12.common.entity.SinologyReading;
import com.k12.resource.service.SinologyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 国学阅读·作文深度融合 API
 * 基础路径：/api/sinology
 *
 * 接口列表：
 *   GET  /api/sinology/unit-bundle     - 核心接口：按单元打包查询国学阅读+作文
 *   GET  /api/sinology/search           - 多维度检索国学素材
 *   GET  /api/sinology/{id}             - 国学阅读详情
 *   GET  /api/sinology/filter-enums     - 体裁/朝代筛选枚举
 *   GET  /api/sinology/schools          - 学校列表查询
 */
@Slf4j
@RestController
@RequestMapping("/api/sinology")
public class SinologyController {

    private final SinologyService sinologyService;
    public SinologyController(SinologyService sinologyService) {
        this.sinologyService = sinologyService;
    }


    // =================== 核心接口 ===================

    /**
     * 按单元获取完整包（国学阅读 + 作文训练 + 学校信息）
     * GET /api/sinology/unit-bundle?unitId=1
     */
    @GetMapping("/unit-bundle")
    public Result<SinologyUnitBundleVO> getUnitBundle(@RequestParam Long unitId) {
        try {
            SinologyUnitBundleVO bundle = sinologyService.getUnitBundle(unitId);
            return Result.success(bundle);
        } catch (Exception e) {
            log.error("获取单元打包数据失败, unitId={}", unitId, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // =================== 检索接口 ===================

    /**
     * 多维度检索国学阅读素材
     * GET /api/sinology/search?gradeName=三年级&editionName=统编版&genre=诗词&keyword=李白
     */
    @GetMapping("/search")
    public Result<List<SinologyReading>> search(
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String editionName,
            @RequestParam(required = false) String volumeName,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) String dynasty,
            @RequestParam(required = false) String keyword) {
        try {
            List<SinologyReading> list = sinologyService.searchSinology(
                    gradeName, editionName, volumeName, genre, dynasty, keyword);
            return Result.success(list);
        } catch (Exception e) {
            log.error("检索国学素材失败", e);
            return Result.error("检索失败：" + e.getMessage());
        }
    }

    /**
     * 国学阅读详情
     * GET /api/sinology/{id}
     */
    @GetMapping("/{id}")
    public Result<SinologyReading> getDetail(@PathVariable Long id) {
        try {
            SinologyReading detail = sinologyService.getSinologyDetail(id);
            if (detail == null) {
                return Result.error("素材不存在");
            }
            return Result.success(detail);
        } catch (Exception e) {
            log.error("获取国学阅读详情失败, id={}", id, e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    /**
     * 获取筛选枚举（体裁、朝代）
     * GET /api/sinology/filter-enums
     */
    @GetMapping("/filter-enums")
    public Result<Map<String, Object>> getFilterEnums() {
        try {
            return Result.success(sinologyService.getFilterEnums());
        } catch (Exception e) {
            log.error("获取筛选枚举失败", e);
            return Result.error("获取失败：" + e.getMessage());
        }
    }

    // =================== 学校接口 ===================

    /**
     * 查询学校列表
     * GET /api/sinology/schools?regionId=510100
     * GET /api/sinology/schools?regionPath=成都
     * GET /api/sinology/schools?tag=国学特色
     */
    @GetMapping("/schools")
    public Result<List<Map<String, Object>>> listSchools(
            @RequestParam(required = false) Integer regionId,
            @RequestParam(required = false) String regionPath,
            @RequestParam(required = false) String tag) {
        try {
            return Result.success(sinologyService.listSchools(regionId, regionPath, tag));
        } catch (Exception e) {
            log.error("查询学校列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
