package com.k12.resource.service;

import com.k12.common.entity.Category;
import com.k12.common.entity.Dict;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    
    // ========== 分类管理 ==========
    List<Category> listCategories();
    
    void createCategory(Category category);
    
    void updateCategory(Category category);
    
    void deleteCategory(Long id);
    
    // ========== 字典管理 ==========
    List<Dict> listDicts(String type);
    
    /**
     * 按学段查询字典
     * @param type 字典类型
     * @param gradeLevel 学段
     * @return 字典列表
     */
    List<Dict> listDictsByGrade(String type, String gradeLevel);
    
    /**
     * 获取学段筛选数据（学科/资源类型/教材版本/备考类型）
     * @param gradeLevel 学段
     * @return 包含各类字典的Map
     */
    Map<String, List<Dict>> getGradeFilters(String gradeLevel);
    
    void createDict(Dict dict);
    
    void updateDict(Dict dict);
    
    void deleteDict(Long id);
}
