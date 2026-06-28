package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.UnitDocQueryDTO;
import com.k12.common.entity.UnitDoc;
import com.k12.resource.service.UnitDocService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单元文档Controller
 * 提供学段资源相关的REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/unit-doc")
public class UnitDocController {
    
    private final UnitDocService unitDocService;
    public UnitDocController(UnitDocService unitDocService) {
        this.unitDocService = unitDocService;
    }

    
    /**
     * 创建单元文档记录
     * POST /api/unit-doc/create
     */
    @PostMapping("/create")
    public Result<UnitDoc> create(@RequestBody UnitDoc unitDoc) {
        try {
            UnitDoc created = unitDocService.create(unitDoc);
            return Result.success(created);
        } catch (Exception e) {
            log.error("创建单元文档失败", e);
            return Result.error("创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据ID查询
     * GET /api/unit-doc/{id}
     */
    @GetMapping("/{id}")
    public Result<UnitDoc> getById(@PathVariable Integer id) {
        UnitDoc unitDoc = unitDocService.getById(id);
        if (unitDoc == null) {
            return Result.error("文档不存在");
        }
        return Result.success(unitDoc);
    }
    
    /**
     * 根据单元名称查询
     * GET /api/unit-doc/by-unit?unitName=第一单元
     */
    @GetMapping("/by-unit")
    public Result<List<UnitDoc>> getByUnitName(@RequestParam String unitName) {
        List<UnitDoc> list = unitDocService.getByUnitName(unitName);
        return Result.success(list);
    }
    
    /**
     * 分页查询
     * GET /api/unit-doc/list?current=1&size=10&unitName=xxx&fileType=doc&sortField=uploadTime&sortOrder=desc
     */
    @GetMapping("/list")
    public Result<Map<String, Object>> listByPage(UnitDocQueryDTO query) {
        try {
            Map<String, Object> result = unitDocService.listByPage(query);
            return Result.success(result);
        } catch (Exception e) {
            log.error("查询单元文档列表失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 查询所有（不分页）
     * GET /api/unit-doc/all?unitName=xxx&fileType=doc
     */
    @GetMapping("/all")
    public Result<List<UnitDoc>> listAll(UnitDocQueryDTO query) {
        try {
            List<UnitDoc> list = unitDocService.listAll(query);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询所有单元文档失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据文件名模糊搜索
     * GET /api/unit-doc/search?filename=知识
     */
    @GetMapping("/search")
    public Result<List<UnitDoc>> searchByFilename(@RequestParam String filename) {
        List<UnitDoc> list = unitDocService.searchByFilename(filename);
        return Result.success(list);
    }
    
    /**
     * 根据文件类型查询
     * GET /api/unit-doc/by-type?fileType=doc
     */
    @GetMapping("/by-type")
    public Result<List<UnitDoc>> getByFileType(@RequestParam String fileType) {
        List<UnitDoc> list = unitDocService.getByFileType(fileType);
        return Result.success(list);
    }
    
    /**
     * 根据文件大小范围查询
     * GET /api/unit-doc/by-size?minKb=10&maxKb=1000
     */
    @GetMapping("/by-size")
    public Result<List<UnitDoc>> getBySizeRange(@RequestParam Integer minKb,
                                                  @RequestParam Integer maxKb) {
        List<UnitDoc> list = unitDocService.getBySizeRange(minKb, maxKb);
        return Result.success(list);
    }
    
    /**
     * 更新单元文档
     * PUT /api/unit-doc/update
     */
    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody UnitDoc unitDoc) {
        try {
            boolean success = unitDocService.update(unitDoc);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新单元文档失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 更新OSS URL
     * PUT /api/unit-doc/update-oss-url?id=1&ossUrl=xxx
     */
    @PutMapping("/update-oss-url")
    public Result<Boolean> updateOssUrl(@RequestParam Integer id,
                                          @RequestParam String ossUrl) {
        try {
            boolean success = unitDocService.updateOssUrl(id, ossUrl);
            return Result.success(success);
        } catch (Exception e) {
            log.error("更新OSS URL失败", e);
            return Result.error("更新失败：" + e.getMessage());
        }
    }
    
    /**
     * 删除单元文档（逻辑删除）
     * DELETE /api/unit-doc/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public Result<Boolean> deleteById(@PathVariable Integer id) {
        try {
            boolean success = unitDocService.deleteById(id);
            return Result.success(success);
        } catch (Exception e) {
            log.error("删除单元文档失败", e);
            return Result.error("删除失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取统计信息
     * GET /api/unit-doc/stats
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = unitDocService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取统计信息失败", e);
            return Result.error("获取统计失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取所有单元名称（去重）
     * GET /api/unit-doc/unit-names
     */
    @GetMapping("/unit-names")
    public Result<List<String>> getAllUnitNames() {
        List<String> unitNames = unitDocService.getAllUnitNames();
        return Result.success(unitNames);
    }
    
    /**
     * 批量导入单元文档
     * POST /api/unit-doc/batch-create
     */
    @PostMapping("/batch-create")
    public Result<Map<String, Object>> batchCreate(@RequestBody List<UnitDoc> unitDocs) {
        try {
            Map<String, Object> result = new HashMap<>();
            int successCount = 0;
            List<String> errors = new ArrayList<>();
            
            for (UnitDoc doc : unitDocs) {
                try {
                    unitDocService.create(doc);
                    successCount++;
                } catch (Exception e) {
                    errors.add("单元：" + doc.getUnitName() + "，文件：" + doc.getOriginalFilename() + "，错误：" + e.getMessage());
                }
            }
            
            result.put("successCount", successCount);
            result.put("totalCount", unitDocs.size());
            result.put("errors", errors);
            
            return Result.success(result);
        } catch (Exception e) {
            log.error("批量创建单元文档失败", e);
            return Result.error("批量创建失败：" + e.getMessage());
        }
    }
    
    /**
     * 根据单元名称获取文档数量
     * GET /api/unit-doc/count-by-unit?unitName=第一单元
     */
    @GetMapping("/count-by-unit")
    public Result<Integer> countByUnitName(@RequestParam String unitName) {
        List<UnitDoc> list = unitDocService.getByUnitName(unitName);
        return Result.success(list.size());
    }
    
    /**
     * 获取所有文档的总大小（MB）
     * GET /api/unit-doc/total-size
     */
    @GetMapping("/total-size")
    public Result<Double> getTotalSize() {
        Map<String, Object> stats = unitDocService.getStatistics();
        Object totalSizeKb = stats.get("total_size_kb");
        if (totalSizeKb == null) {
            return Result.success(0.0);
        }
        
        Double totalMb = Math.round(Integer.parseInt(totalSizeKb.toString()) / 1024.0 * 100.0) / 100.0;
        return Result.success(totalMb);
    }
}
