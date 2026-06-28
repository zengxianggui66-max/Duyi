package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.entity.Category;
import com.k12.common.entity.Dict;
import com.k12.resource.mapper.CategoryMapper;
import com.k12.resource.mapper.DictMapper;
import com.k12.resource.service.CategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Service
@SuppressWarnings("null")
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final DictMapper dictMapper;
    public CategoryServiceImpl(CategoryMapper categoryMapper, DictMapper dictMapper) {
        this.categoryMapper = categoryMapper;
        this.dictMapper = dictMapper;
    }


    // ========== 分类管理 ==========

    @Override
    public List<Category> listCategories() {
        return categoryMapper.selectList(
            new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByAsc(Category::getSort)
        );
    }

    @Override
    @Transactional
    public void createCategory(Category category) {
        category.setStatus(1);
        categoryMapper.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(Category category) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException("分类不存在");
        }
        existing.setName(category.getName());
        existing.setIcon(category.getIcon());
        existing.setSort(category.getSort());
        categoryMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    // ========== 字典管理 ==========

    @Override
    public List<Dict> listDicts(String type) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getStatus, 1);
        if (type != null) {
            wrapper.eq(Dict::getType, type);
        }
        wrapper.orderByAsc(Dict::getSort);
        return dictMapper.selectList(wrapper);
    }

    /**
     * 按学段查询字典
     */
    @Override
    public List<Dict> listDictsByGrade(String type, String gradeLevel) {
        LambdaQueryWrapper<Dict> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Dict::getStatus, 1);
        
        if (StringUtils.hasText(type)) {
            wrapper.eq(Dict::getType, type);
        }
        
        // 如果指定了学段，需要匹配 grade_levels 字段
        if (StringUtils.hasText(gradeLevel)) {
            wrapper.and(w -> w
                .isNull(Dict::getGradeLevels)
                .or()
                .like(Dict::getGradeLevels, gradeLevel)
            );
        }
        
        wrapper.orderByAsc(Dict::getSort);
        return dictMapper.selectList(wrapper);
    }

    /**
     * 获取学段筛选数据（学科/资源类型/教材版本/备考类型）
     */
    @Override
    public Map<String, List<Dict>> getGradeFilters(String gradeLevel) {
        Map<String, List<Dict>> filters = new LinkedHashMap<>();
        
        // 学段信息
        filters.put("gradeLevels", listDictsByGrade("grade_level", null));
        
        // 学科（按学段筛选）
        filters.put("subjects", listDictsByGrade("subject", gradeLevel));
        
        // 资源类型（按学段筛选）
        filters.put("resourceTypes", listDictsByGrade("resource_type", gradeLevel));
        
        // 教材版本（按学段筛选）
        filters.put("textbookVersions", listDictsByGrade("textbook_version", gradeLevel));
        
        // 备考类型（按学段筛选）
        filters.put("examTypes", listDictsByGrade("exam_type", gradeLevel));
        
        return filters;
    }

    @Override
    @Transactional
    public void createDict(Dict dict) {
        dict.setStatus(1);
        dictMapper.insert(dict);
    }

    @Override
    @Transactional
    public void updateDict(Dict dict) {
        Dict existing = dictMapper.selectById(dict.getId());
        if (existing == null) {
            throw new BusinessException("字典项不存在");
        }
        existing.setLabel(dict.getLabel());
        existing.setValue(dict.getValue());
        existing.setSort(dict.getSort());
        dictMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteDict(Long id) {
        dictMapper.deleteById(id);
    }
}
