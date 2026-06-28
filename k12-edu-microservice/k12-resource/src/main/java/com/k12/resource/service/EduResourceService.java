package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.dto.EduResourceQueryDTO;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.EduResourceType;
import com.k12.resource.mapper.EduModuleMapper;
import com.k12.resource.mapper.EduResourceMapper;
import com.k12.resource.mapper.EduResourceTypeMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * edu_resource 资源 Service
 * 支持多维度（学段/学科/版本/年级/学期/册别/栏目/类型/单元）的资源查询
 */
@Slf4j
@Service
public class EduResourceService {

    private final EduResourceMapper eduResourceMapper;
    private final EduModuleMapper eduModuleMapper;
    private final EduResourceTypeMapper eduResourceTypeMapper;
    private final SearchIndexSyncHook searchIndexSyncHook;
    public EduResourceService(EduResourceMapper eduResourceMapper, EduModuleMapper eduModuleMapper, EduResourceTypeMapper eduResourceTypeMapper, SearchIndexSyncHook searchIndexSyncHook) {
        this.eduResourceMapper = eduResourceMapper;
        this.eduModuleMapper = eduModuleMapper;
        this.eduResourceTypeMapper = eduResourceTypeMapper;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    // ===================== 白名单排序字段 =====================

    private static final Map<String, String> SORT_FIELD_WHITELIST = new HashMap<>();

    static {
        SORT_FIELD_WHITELIST.put("uploadTime", "r.upload_time");
        SORT_FIELD_WHITELIST.put("upload_time", "r.upload_time");
        SORT_FIELD_WHITELIST.put("downloadCount", "r.download_count");
        SORT_FIELD_WHITELIST.put("download_count", "r.download_count");
        SORT_FIELD_WHITELIST.put("viewCount", "r.view_count");
        SORT_FIELD_WHITELIST.put("view_count", "r.view_count");
        SORT_FIELD_WHITELIST.put("collectCount", "r.collect_count");
        SORT_FIELD_WHITELIST.put("collect_count", "r.collect_count");
        SORT_FIELD_WHITELIST.put("fileSizeKb", "r.file_size_kb");
        SORT_FIELD_WHITELIST.put("file_size_kb", "r.file_size_kb");
        SORT_FIELD_WHITELIST.put("sort", "r.sort");
    }

    // ===================== 查询 =====================

    /**
     * 分页查询资源（带完整维度信息）
     */
    public Map<String, Object> listByPage(EduResourceQueryDTO dto) {
        String sortField = SORT_FIELD_WHITELIST.getOrDefault(dto.getSortField(), "r.upload_time");
        String sortOrder = "asc".equalsIgnoreCase(dto.getSortOrder()) ? "asc" : "desc";
        // 默认只查已发布
        Integer status = dto.getStatus() != null ? dto.getStatus() : 1;

        Page<EduResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        eduResourceMapper.findByDimensionPage(
                page,
                dto.getStageId(), dto.getStageName(),
                dto.getSubjectId(), dto.getSubjectName(),
                dto.getEditionId(), dto.getEditionName(),
                dto.getGradeId(), dto.getGradeName(),
                dto.getSemesterId(), dto.getSemesterName(),
                dto.getVolumeId(), dto.getVolumeName(),
                dto.getModuleId(), dto.getModuleName(),
                dto.getResourceTypeId(), dto.getResourceTypeName(),
                dto.getUnitId(), dto.getUnitName(),
                dto.getFileExt(), status, dto.getKeyword(),
                sortField, sortOrder
        );

        Map<String, Object> result = new HashMap<>();
        result.put("records", page.getRecords());
        result.put("total", page.getTotal());
        result.put("current", page.getCurrent());
        result.put("size", page.getSize());
        result.put("pages", page.getPages());
        return result;
    }

    /**
     * 不分页查询资源列表（适合成套资源/树形导航）
     */
    public List<EduResource> listAll(EduResourceQueryDTO dto) {
        Integer status = dto.getStatus() != null ? dto.getStatus() : 1;
        return eduResourceMapper.findByDimension(
                dto.getStageId(), dto.getStageName(),
                dto.getSubjectId(), dto.getSubjectName(),
                dto.getEditionId(), dto.getEditionName(),
                dto.getGradeId(), dto.getGradeName(),
                dto.getSemesterId(), dto.getSemesterName(),
                dto.getVolumeId(), dto.getVolumeName(),
                dto.getModuleId(), dto.getModuleName(),
                dto.getResourceTypeId(), dto.getResourceTypeName(),
                dto.getUnitId(), dto.getUnitName(),
                dto.getFileExt(), status, dto.getKeyword()
        );
    }

    /**
     * 根据ID查询详情
     */
    public EduResource getById(Long id) {
        return eduResourceMapper.selectById(id);
    }

    // ===================== 筛选枚举 =====================

    /**
     * 获取所有学段（有资源数据的）
     */
    public List<Map<String, Object>> getStages() {
        return eduResourceMapper.findDistinctStages();
    }

    /**
     * 获取学科列表（按学段过滤）
     */
    public List<Map<String, Object>> getSubjects(String stageName) {
        return eduResourceMapper.findDistinctSubjects(stageName);
    }

    /**
     * 获取版本列表
     */
    public List<Map<String, Object>> getEditions(String stageName, String subjectName) {
        return eduResourceMapper.findDistinctEditions(stageName, subjectName);
    }

    /**
     * 获取年级列表
     */
    public List<Map<String, Object>> getGrades(String stageName) {
        return eduResourceMapper.findDistinctGrades(stageName);
    }

    /**
     * 获取栏目列表（直接从 edu_module 表查询，包含 icon）
     */
    public List<Map<String, Object>> getModules(String stageName, String subjectName) {
        String stage = (stageName != null && !stageName.isBlank()) ? stageName : "小学";
        List<Map<String, Object>> rows = eduModuleMapper.findByStageName(stage);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", row.get("id"));
            map.put("name", row.get("name"));
            map.put("icon", row.get("icon"));
            map.put("sort", row.get("sort"));
            result.add(map);
        }
        return result;
    }

    /**
     * 获取资源类型列表（仅二级叶子类型，不含「备课授课」等一级分组名）
     */
    public List<Map<String, Object>> getResourceTypes(String stageName) {
        return getBrowseResourceTypes(stageName, null, null);
    }

    /**
     * 首页/学科浏览：优先返回资源库中该学段+学科+栏目下已有数据的类型；无数据时返回备课授课类叶子类型
     */
    public List<Map<String, Object>> getBrowseResourceTypes(
            String stageName, String subjectName, String moduleName) {
        List<Map<String, Object>> fromResources = eduResourceMapper.findDistinctResourceTypes(
                stageName, subjectName, moduleName);
        if (fromResources != null && !fromResources.isEmpty()) {
            return fromResources;
        }
        QueryWrapper<EduResourceType> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        wrapper.gt("parent_id", 0);
        if (moduleName != null && moduleName.contains("同步")) {
            wrapper.eq("group_code", "teach");
        }
        wrapper.orderByAsc("sort");
        List<EduResourceType> types = eduResourceTypeMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EduResourceType t : types) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", t.getId());
            map.put("name", t.getName());
            map.put("icon", t.getIcon());
            map.put("sort", t.getSort());
            result.add(map);
        }
        return result;
    }

    /**
     * 获取单元列表（按年级/版本/册别/学科过滤，供侧边树）
     */
    public List<Map<String, Object>> getUnits(String gradeName, String editionName, String volumeName, String subjectName) {
        return eduResourceMapper.findDistinctUnits(gradeName, editionName, volumeName, subjectName);
    }

    /**
     * 一次性获取所有筛选枚举项
     * 返回：{ stages, subjects, editions, grades, modules, resourceTypes }
     */
    public Map<String, Object> getFilterOptions(String stageName, String subjectName) {
        Map<String, Object> options = new HashMap<>();
        options.put("stages", eduResourceMapper.findDistinctStages());
        options.put("subjects", eduResourceMapper.findDistinctSubjects(stageName));
        options.put("editions", eduResourceMapper.findDistinctEditions(stageName, subjectName));
        options.put("grades", eduResourceMapper.findDistinctGrades(stageName));
        options.put("modules", getModules(stageName, subjectName));
        options.put("resourceTypes", getBrowseResourceTypes(stageName, subjectName, "同步备课"));
        return options;
    }

    /**
     * 获取各栏目下的资源统计数量
     */
    public List<Map<String, Object>> countByModule(String stageName, String subjectName,
                                                    String gradeName, String editionName) {
        return eduResourceMapper.countByModule(stageName, subjectName, gradeName, editionName);
    }

    // ===================== 写操作 =====================

    /**
     * 新增资源
     */
    public EduResource save(EduResource resource) {
        eduResourceMapper.insert(resource);
        searchIndexSyncHook.afterEduResourceChanged(resource.getId());
        return resource;
    }

    /**
     * 更新资源
     */
    public EduResource update(EduResource resource) {
        eduResourceMapper.updateById(resource);
        searchIndexSyncHook.afterEduResourceChanged(resource.getId());
        return resource;
    }

    /**
     * 增加下载次数（原子操作，线程安全）
     */
    public void incrementDownloadCount(Long id) {
        EduResource resource = getById(id);
        if (resource != null) {
            resource.setDownloadCount((resource.getDownloadCount() == null ? 0 : resource.getDownloadCount()) + 1);
            eduResourceMapper.updateById(resource);
        }
    }

    /**
     * 增加浏览次数
     */
    public void incrementViewCount(Long id) {
        EduResource resource = getById(id);
        if (resource != null) {
            resource.setViewCount((resource.getViewCount() == null ? 0 : resource.getViewCount()) + 1);
            eduResourceMapper.updateById(resource);
        }
    }

    /**
     * 增加收藏次数
     */
    public void incrementCollectCount(Long id) {
        EduResource resource = getById(id);
        if (resource != null) {
            resource.setCollectCount((resource.getCollectCount() == null ? 0 : resource.getCollectCount()) + 1);
            eduResourceMapper.updateById(resource);
        }
    }
}
