package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.entity.Category;
import com.k12.common.entity.Dict;
import com.k12.resource.service.CategoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 分类管理接口
 */
@RestController
@RequestMapping("/api")
public class CategoryController {

    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }


    /**
     * 获取分类列表
     */
    @GetMapping("/category/list")
    public Result<List<Category>> listCategories() {
        return Result.success(categoryService.listCategories());
    }

    /**
     * 创建分类（管理端）
     */
    @PostMapping("/admin/category")
    public Result<Void> create(@RequestBody Category category) {
        categoryService.createCategory(category);
        return Result.success(null);
    }

    /**
     * 更新分类（管理端）
     */
    @PutMapping("/admin/category/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Category category) {
        category.setId(id);
        categoryService.updateCategory(category);
        return Result.success(null);
    }

    /**
     * 删除分类（管理端）
     */
    @DeleteMapping("/admin/category/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success(null);
    }

    /**
     * 获取字典列表（全部）
     */
    @GetMapping("/dict/list")
    public Result<List<Dict>> listDicts(@RequestParam(required = false) String type) {
        return Result.success(categoryService.listDicts(type));
    }

    /**
     * 按学段查询字典
     */
    @GetMapping("/dict/grade")
    public Result<List<Dict>> listDictsByGrade(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gradeLevel) {
        return Result.success(categoryService.listDictsByGrade(type, gradeLevel));
    }

    /**
     * 获取学段筛选数据（学科/资源类型/教材版本/备考类型）
     * 用于学段专区页面的筛选
     */
    @GetMapping("/dict/filters")
    public Result<Map<String, List<Dict>>> getGradeFilters(@RequestParam String gradeLevel) {
        return Result.success(categoryService.getGradeFilters(gradeLevel));
    }

    /**
     * 创建字典（管理端）
     */
    @PostMapping("/admin/dict")
    public Result<Void> createDict(@RequestBody Dict dict) {
        categoryService.createDict(dict);
        return Result.success(null);
    }

    /**
     * 更新字典（管理端）
     */
    @PutMapping("/admin/dict/{id}")
    public Result<Void> updateDict(@PathVariable Long id, @RequestBody Dict dict) {
        dict.setId(id);
        categoryService.updateDict(dict);
        return Result.success(null);
    }

    /**
     * 删除字典（管理端）
     */
    @DeleteMapping("/admin/dict/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        categoryService.deleteDict(id);
        return Result.success(null);
    }
}
