package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.*;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.*;
import com.k12.resource.search.TaxonomySearchSyncHook;
import com.k12.resource.service.AdminTaxonomyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AdminTaxonomyServiceImpl implements AdminTaxonomyService {

    private static final String PERM_EDIT = "admin:taxonomy:edit";

    private final EduStageMapper eduStageMapper;
    private final EduSubjectMapper eduSubjectMapper;
    private final EduEditionMapper eduEditionMapper;
    private final EduGradeMapper eduGradeMapper;
    private final EduVolumeMapper eduVolumeMapper;
    private final EduModuleMapper eduModuleMapper;
    private final EduResourceTypeMapper eduResourceTypeMapper;
    private final EduSubjectEditionMapper eduSubjectEditionMapper;
    private final EduModuleStageMapper eduModuleStageMapper;
    private final EduModuleSubjectMapper eduModuleSubjectMapper;
    private final EduSubjectResourceTypeMapper eduSubjectResourceTypeMapper;
    private final AdminPermissionService adminPermissionService;
    private final TaxonomySearchSyncHook taxonomySearchSyncHook;
    /** Phase 6：资源引用检查 */
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    public AdminTaxonomyServiceImpl(EduStageMapper eduStageMapper, EduSubjectMapper eduSubjectMapper, EduEditionMapper eduEditionMapper, EduGradeMapper eduGradeMapper, EduVolumeMapper eduVolumeMapper, EduModuleMapper eduModuleMapper, EduResourceTypeMapper eduResourceTypeMapper, EduSubjectEditionMapper eduSubjectEditionMapper, EduModuleStageMapper eduModuleStageMapper, EduModuleSubjectMapper eduModuleSubjectMapper, EduSubjectResourceTypeMapper eduSubjectResourceTypeMapper, AdminPermissionService adminPermissionService, TaxonomySearchSyncHook taxonomySearchSyncHook, PrimaryChineseResourceMapper primaryChineseResourceMapper) {
        this.eduStageMapper = eduStageMapper;
        this.eduSubjectMapper = eduSubjectMapper;
        this.eduEditionMapper = eduEditionMapper;
        this.eduGradeMapper = eduGradeMapper;
        this.eduVolumeMapper = eduVolumeMapper;
        this.eduModuleMapper = eduModuleMapper;
        this.eduResourceTypeMapper = eduResourceTypeMapper;
        this.eduSubjectEditionMapper = eduSubjectEditionMapper;
        this.eduModuleStageMapper = eduModuleStageMapper;
        this.eduModuleSubjectMapper = eduModuleSubjectMapper;
        this.eduSubjectResourceTypeMapper = eduSubjectResourceTypeMapper;
        this.adminPermissionService = adminPermissionService;
        this.taxonomySearchSyncHook = taxonomySearchSyncHook;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
    }


    @Override
    public List<EduStage> listStages(boolean includeDisabled) {
        LambdaQueryWrapper<EduStage> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduStage::getStatus, 1);
        }
        wrapper.orderByAsc(EduStage::getSort).orderByAsc(EduStage::getId);
        return eduStageMapper.selectList(wrapper);
    }

    @Override
    public EduStage createStage(AdminTaxonomyStageWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueStageCode(dto.getCode(), null);
        EduStage stage = new EduStage();
        applyStage(stage, dto);
        stage.setCreateTime(LocalDateTime.now());
        stage.setUpdateTime(LocalDateTime.now());
        eduStageMapper.insert(stage);
        notifySearchSync();
        return stage;
    }

    @Override
    public EduStage updateStage(Integer id, AdminTaxonomyStageWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduStage stage = requireStage(id);
        assertUniqueStageCode(dto.getCode(), id);
        applyStage(stage, dto);
        stage.setUpdateTime(LocalDateTime.now());
        eduStageMapper.updateById(stage);
        notifySearchSync();
        return stage;
    }

    @Override
    public void setStageStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduStage stage = requireStage(id);
        stage.setStatus(normalizeStatus(status));
        stage.setUpdateTime(LocalDateTime.now());
        eduStageMapper.updateById(stage);
        notifySearchSync();
    }

    @Override
    public void deleteStage(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        EduStage stage = requireStage(id);
        // Phase 6：检查子学科
        Long subjectCount = eduSubjectMapper.selectCount(new LambdaQueryWrapper<EduSubject>()
                .eq(EduSubject::getStageId, id));
        if (subjectCount != null && subjectCount > 0) {
            throw new BusinessException(400, "该学段下仍有学科，无法删除");
        }
        // Phase 6：检查是否有资源引用
        long refCount = primaryChineseResourceMapper.countByStage(stage.getCode());
        if (refCount > 0) {
            throw new BusinessException(400,
                    String.format("该学段已被 %d 个资源引用，无法删除，建议改为禁用", refCount));
        }
        eduStageMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<AdminTaxonomySubjectAdminVO> listSubjects(Integer stageId, boolean includeDisabled) {
        LambdaQueryWrapper<EduSubject> wrapper = new LambdaQueryWrapper<>();
        if (stageId != null) {
            wrapper.eq(EduSubject::getStageId, stageId);
        }
        if (!includeDisabled) {
            wrapper.eq(EduSubject::getStatus, 1);
        }
        wrapper.orderByAsc(EduSubject::getSort).orderByAsc(EduSubject::getId);
        List<EduSubject> rows = eduSubjectMapper.selectList(wrapper);
        List<AdminTaxonomySubjectAdminVO> result = new ArrayList<>(rows.size());
        for (EduSubject row : rows) {
            EduStage stage = row.getStageId() != null ? eduStageMapper.selectById(row.getStageId()) : null;
            List<Integer> editionIds = eduSubjectEditionMapper.selectEditionIdsBySubjectId(row.getId());
            List<Integer> moduleIds = eduModuleSubjectMapper.selectModuleIdsBySubjectId(row.getId());
            List<Integer> resourceTypeIds = eduSubjectResourceTypeMapper.selectTypeIdsBySubjectId(row.getId());
            result.add(AdminTaxonomySubjectAdminVO.from(
                    row,
                    stage != null ? stage.getCode() : null,
                    stage != null ? stage.getName() : null,
                    editionIds,
                    moduleIds,
                    resourceTypeIds));
        }
        return result;
    }

    @Override
    @Transactional
    public AdminTaxonomySubjectAdminVO createSubject(AdminTaxonomySubjectWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        requireStage(dto.getStageId());
        EduSubject subject = new EduSubject();
        applySubject(subject, dto);
        subject.setCreateTime(LocalDateTime.now());
        subject.setUpdateTime(LocalDateTime.now());
        eduSubjectMapper.insert(subject);
        syncSubjectEditions(subject.getId(), dto.getEditionIds());
        syncSubjectModules(subject.getId(), dto.getModuleIds());
        syncSubjectResourceTypes(subject.getId(), dto.getResourceTypeIds());
        notifySearchSync();
        return toSubjectVo(subject);
    }

    @Override
    @Transactional
    public AdminTaxonomySubjectAdminVO updateSubject(Integer id, AdminTaxonomySubjectWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduSubject subject = requireSubject(id);
        requireStage(dto.getStageId());
        applySubject(subject, dto);
        subject.setUpdateTime(LocalDateTime.now());
        eduSubjectMapper.updateById(subject);
        syncSubjectEditions(subject.getId(), dto.getEditionIds());
        syncSubjectModules(subject.getId(), dto.getModuleIds());
        syncSubjectResourceTypes(subject.getId(), dto.getResourceTypeIds());
        notifySearchSync();
        return toSubjectVo(subject);
    }

    @Override
    public void setSubjectStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduSubject subject = requireSubject(id);
        subject.setStatus(normalizeStatus(status));
        subject.setUpdateTime(LocalDateTime.now());
        eduSubjectMapper.updateById(subject);
        notifySearchSync();
    }

    @Override
    @Transactional
    public void deleteSubject(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        EduSubject subject = requireSubject(id);
        // Phase 6：检查是否有资源引用
        long refCount = primaryChineseResourceMapper.countBySubject(subject.getCode());
        if (refCount > 0) {
            throw new BusinessException(400,
                    String.format("该学科已被 %d 个资源引用，无法删除，建议改为禁用", refCount));
        }
        eduSubjectEditionMapper.deleteBySubjectId(id);
        eduModuleSubjectMapper.deleteBySubjectId(id);
        eduSubjectResourceTypeMapper.deleteBySubjectId(id);
        eduSubjectMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<EduEdition> listEditions(Integer stageId, Integer subjectId, boolean includeDisabled) {
        LambdaQueryWrapper<EduEdition> wrapper = new LambdaQueryWrapper<>();
        if (stageId != null) {
            wrapper.and(w -> w.eq(EduEdition::getStageId, stageId).or().isNull(EduEdition::getStageId));
        }
        if (subjectId != null) {
            wrapper.and(w -> w.eq(EduEdition::getSubjectId, subjectId).or().isNull(EduEdition::getSubjectId));
        }
        if (!includeDisabled) {
            wrapper.eq(EduEdition::getStatus, 1);
        }
        wrapper.orderByAsc(EduEdition::getSort).orderByAsc(EduEdition::getId);
        return eduEditionMapper.selectList(wrapper);
    }

    @Override
    public EduEdition createEdition(AdminTaxonomyEditionWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduEdition edition = new EduEdition();
        applyEdition(edition, dto);
        edition.setCreateTime(LocalDateTime.now());
        edition.setUpdateTime(LocalDateTime.now());
        eduEditionMapper.insert(edition);
        notifySearchSync();
        return edition;
    }

    @Override
    public EduEdition updateEdition(Integer id, AdminTaxonomyEditionWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduEdition edition = requireEdition(id);
        applyEdition(edition, dto);
        edition.setUpdateTime(LocalDateTime.now());
        eduEditionMapper.updateById(edition);
        notifySearchSync();
        return edition;
    }

    @Override
    public void setEditionStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduEdition edition = requireEdition(id);
        edition.setStatus(normalizeStatus(status));
        edition.setUpdateTime(LocalDateTime.now());
        eduEditionMapper.updateById(edition);
        notifySearchSync();
    }

    @Override
    public void deleteEdition(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        EduEdition edition = requireEdition(id);
        // Phase 6：检查是否有资源引用
        long refCount = primaryChineseResourceMapper.countByEdition(edition.getCode());
        if (refCount > 0) {
            throw new BusinessException(400,
                    String.format("该版本已被 %d 个资源引用，无法删除，建议改为禁用", refCount));
        }
        eduSubjectEditionMapper.deleteByEditionId(id);
        eduEditionMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<EduGrade> listGrades(Integer stageId, boolean includeDisabled) {
        LambdaQueryWrapper<EduGrade> wrapper = new LambdaQueryWrapper<>();
        if (stageId != null) {
            wrapper.eq(EduGrade::getStageId, stageId);
        }
        if (!includeDisabled) {
            wrapper.eq(EduGrade::getStatus, 1);
        }
        wrapper.orderByAsc(EduGrade::getSort).orderByAsc(EduGrade::getId);
        return eduGradeMapper.selectList(wrapper);
    }

    @Override
    public EduGrade createGrade(AdminTaxonomyGradeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        requireStage(dto.getStageId());
        EduGrade grade = new EduGrade();
        applyGrade(grade, dto);
        grade.setCreateTime(LocalDateTime.now());
        grade.setUpdateTime(LocalDateTime.now());
        eduGradeMapper.insert(grade);
        notifySearchSync();
        return grade;
    }

    @Override
    public EduGrade updateGrade(Integer id, AdminTaxonomyGradeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduGrade grade = requireGrade(id);
        requireStage(dto.getStageId());
        applyGrade(grade, dto);
        grade.setUpdateTime(LocalDateTime.now());
        eduGradeMapper.updateById(grade);
        notifySearchSync();
        return grade;
    }

    @Override
    public void setGradeStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduGrade grade = requireGrade(id);
        grade.setStatus(normalizeStatus(status));
        grade.setUpdateTime(LocalDateTime.now());
        eduGradeMapper.updateById(grade);
        notifySearchSync();
    }

    @Override
    public void deleteGrade(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        EduGrade grade = requireGrade(id);
        // Phase 6：检查是否有资源引用（按年级名称查）
        long refCount = primaryChineseResourceMapper.countByGrade(grade.getName());
        if (refCount > 0) {
            throw new BusinessException(400,
                    String.format("该年级已被 %d 个资源引用，无法删除，建议改为禁用", refCount));
        }
        eduGradeMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<EduVolume> listVolumes(Integer stageId, Integer subjectId, Integer editionId, boolean includeDisabled) {
        LambdaQueryWrapper<EduVolume> wrapper = new LambdaQueryWrapper<>();
        if (stageId != null) {
            wrapper.and(w -> w.eq(EduVolume::getStageId, stageId).or().isNull(EduVolume::getStageId));
        }
        if (subjectId != null) {
            wrapper.and(w -> w.eq(EduVolume::getSubjectId, subjectId).or().isNull(EduVolume::getSubjectId));
        }
        if (editionId != null) {
            wrapper.and(w -> w.eq(EduVolume::getEditionId, editionId).or().isNull(EduVolume::getEditionId));
        }
        if (!includeDisabled) {
            wrapper.eq(EduVolume::getStatus, 1);
        }
        wrapper.orderByAsc(EduVolume::getSort).orderByAsc(EduVolume::getId);
        return eduVolumeMapper.selectList(wrapper);
    }

    @Override
    public EduVolume createVolume(AdminTaxonomyVolumeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduVolume volume = new EduVolume();
        applyVolume(volume, dto);
        eduVolumeMapper.insert(volume);
        notifySearchSync();
        return volume;
    }

    @Override
    public EduVolume updateVolume(Integer id, AdminTaxonomyVolumeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduVolume volume = requireVolume(id);
        applyVolume(volume, dto);
        eduVolumeMapper.updateById(volume);
        notifySearchSync();
        return volume;
    }

    @Override
    public void setVolumeStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduVolume volume = requireVolume(id);
        volume.setStatus(normalizeStatus(status));
        eduVolumeMapper.updateById(volume);
        notifySearchSync();
    }

    @Override
    public void deleteVolume(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireVolume(id);
        // Phase 6：册别通过目录方案间接引用，此处不强制检查（edu_unit 已有 fk 约束）
        eduVolumeMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<AdminTaxonomyModuleAdminVO> listModules(Integer stageId, boolean includeDisabled) {
        List<EduModule> modules;
        if (stageId != null) {
            List<Map<String, Object>> rows = eduModuleMapper.findByStageId(stageId, includeDisabled);
            Set<Integer> ids = rows.stream()
                    .map(r -> Integer.valueOf(String.valueOf(r.get("id"))))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            modules = ids.isEmpty() ? List.of() : eduModuleMapper.selectList(
                    new LambdaQueryWrapper<EduModule>().in(EduModule::getId, ids));
        } else {
            LambdaQueryWrapper<EduModule> wrapper = new LambdaQueryWrapper<>();
            if (!includeDisabled) {
                wrapper.eq(EduModule::getStatus, 1);
            }
            wrapper.orderByAsc(EduModule::getSort).orderByAsc(EduModule::getId);
            modules = eduModuleMapper.selectList(wrapper);
        }
        List<AdminTaxonomyModuleAdminVO> result = new ArrayList<>(modules.size());
        for (EduModule module : modules) {
            result.add(toModuleVo(module));
        }
        return result;
    }

    @Override
    @Transactional
    public AdminTaxonomyModuleAdminVO createModule(AdminTaxonomyModuleWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduModule module = new EduModule();
        applyModule(module, dto);
        eduModuleMapper.insert(module);
        syncModuleStages(module.getId(), dto.getStageIds());
        notifySearchSync();
        return toModuleVo(module);
    }

    @Override
    @Transactional
    public AdminTaxonomyModuleAdminVO updateModule(Integer id, AdminTaxonomyModuleWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduModule module = requireModule(id);
        applyModule(module, dto);
        eduModuleMapper.updateById(module);
        syncModuleStages(module.getId(), dto.getStageIds());
        notifySearchSync();
        return toModuleVo(module);
    }

    @Override
    public void setModuleStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduModule module = requireModule(id);
        module.setStatus(normalizeStatus(status));
        eduModuleMapper.updateById(module);
        notifySearchSync();
    }

    @Override
    @Transactional
    public void deleteModule(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireModule(id);
        eduModuleStageMapper.deleteByModuleId(id);
        eduModuleMapper.deleteById(id);
        notifySearchSync();
    }

    @Override
    public List<EduResourceType> listResourceTypes(Integer parentId, boolean includeDisabled) {
        LambdaQueryWrapper<EduResourceType> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(EduResourceType::getParentId, parentId);
        }
        if (!includeDisabled) {
            wrapper.eq(EduResourceType::getStatus, 1);
        }
        wrapper.orderByAsc(EduResourceType::getSort).orderByAsc(EduResourceType::getId);
        return eduResourceTypeMapper.selectList(wrapper);
    }

    @Override
    public EduResourceType createResourceType(AdminTaxonomyResourceTypeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduResourceType type = new EduResourceType();
        applyResourceType(type, dto);
        eduResourceTypeMapper.insert(type);
        notifySearchSync();
        return type;
    }

    @Override
    public EduResourceType updateResourceType(Integer id, AdminTaxonomyResourceTypeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduResourceType type = requireResourceType(id);
        applyResourceType(type, dto);
        eduResourceTypeMapper.updateById(type);
        notifySearchSync();
        return type;
    }

    @Override
    public void setResourceTypeStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduResourceType type = requireResourceType(id);
        type.setStatus(normalizeStatus(status));
        eduResourceTypeMapper.updateById(type);
        notifySearchSync();
    }

    @Override
    public void deleteResourceType(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        EduResourceType type = requireResourceType(id);
        Long childCount = eduResourceTypeMapper.selectCount(new LambdaQueryWrapper<EduResourceType>()
                .eq(EduResourceType::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(400, "该类型下仍有子节点，无法删除");
        }
        if (type.getParentId() == null || type.getParentId() <= 0) {
            throw new BusinessException(400, "一级分组不可删除，请删除其下叶子类型");
        }
        eduResourceTypeMapper.deleteById(id);
        notifySearchSync();
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无分类目录编辑权限");
        }
    }

    private EduStage requireStage(Integer id) {
        EduStage stage = eduStageMapper.selectById(id);
        if (stage == null) {
            throw new BusinessException(404, "学段不存在");
        }
        return stage;
    }

    private EduSubject requireSubject(Integer id) {
        EduSubject subject = eduSubjectMapper.selectById(id);
        if (subject == null) {
            throw new BusinessException(404, "学科不存在");
        }
        return subject;
    }

    private EduEdition requireEdition(Integer id) {
        EduEdition edition = eduEditionMapper.selectById(id);
        if (edition == null) {
            throw new BusinessException(404, "教材版本不存在");
        }
        return edition;
    }

    private EduGrade requireGrade(Integer id) {
        EduGrade grade = eduGradeMapper.selectById(id);
        if (grade == null) {
            throw new BusinessException(404, "年级不存在");
        }
        return grade;
    }

    private EduVolume requireVolume(Integer id) {
        EduVolume volume = eduVolumeMapper.selectById(id);
        if (volume == null) {
            throw new BusinessException(404, "册别不存在");
        }
        return volume;
    }

    private EduModule requireModule(Integer id) {
        EduModule module = eduModuleMapper.selectById(id);
        if (module == null) {
            throw new BusinessException(404, "栏目不存在");
        }
        return module;
    }

    private EduResourceType requireResourceType(Integer id) {
        EduResourceType type = eduResourceTypeMapper.selectById(id);
        if (type == null) {
            throw new BusinessException(404, "资源类型不存在");
        }
        return type;
    }

    private AdminTaxonomySubjectAdminVO toSubjectVo(EduSubject subject) {
        EduStage stage = subject.getStageId() != null ? eduStageMapper.selectById(subject.getStageId()) : null;
        List<Integer> editionIds = eduSubjectEditionMapper.selectEditionIdsBySubjectId(subject.getId());
        List<Integer> moduleIds = eduModuleSubjectMapper.selectModuleIdsBySubjectId(subject.getId());
        List<Integer> resourceTypeIds = eduSubjectResourceTypeMapper.selectTypeIdsBySubjectId(subject.getId());
        return AdminTaxonomySubjectAdminVO.from(
                subject,
                stage != null ? stage.getCode() : null,
                stage != null ? stage.getName() : null,
                editionIds,
                moduleIds,
                resourceTypeIds);
    }

    private AdminTaxonomyModuleAdminVO toModuleVo(EduModule module) {
        List<Map<String, Object>> stageRows = eduModuleStageMapper.selectStageRowsByModuleId(module.getId());
        List<Integer> stageIds = new ArrayList<>();
        List<String> stageNames = new ArrayList<>();
        for (Map<String, Object> row : stageRows) {
            stageIds.add(Integer.valueOf(String.valueOf(row.get("stageId"))));
            stageNames.add(String.valueOf(row.get("stageName")));
        }
        return AdminTaxonomyModuleAdminVO.from(module, stageIds, stageNames);
    }

    private void syncSubjectEditions(Integer subjectId, List<Integer> editionIds) {
        eduSubjectEditionMapper.deleteBySubjectId(subjectId);
        if (editionIds == null || editionIds.isEmpty()) {
            return;
        }
        int sort = 0;
        for (Integer editionId : editionIds) {
            if (editionId == null) {
                continue;
            }
            requireEdition(editionId);
            eduSubjectEditionMapper.insertLink(subjectId, editionId, ++sort);
        }
    }

    private void syncSubjectModules(Integer subjectId, List<Integer> moduleIds) {
        if (moduleIds == null) {
            return;
        }
        eduModuleSubjectMapper.deleteBySubjectId(subjectId);
        if (moduleIds.isEmpty()) {
            return;
        }
        int sort = 0;
        for (Integer moduleId : moduleIds) {
            if (moduleId == null) {
                continue;
            }
            requireModule(moduleId);
            eduModuleSubjectMapper.insertLink(moduleId, subjectId, ++sort, 1);
        }
    }

    private void syncSubjectResourceTypes(Integer subjectId, List<Integer> resourceTypeIds) {
        if (resourceTypeIds == null) {
            return;
        }
        eduSubjectResourceTypeMapper.deleteBySubjectId(subjectId);
        if (resourceTypeIds.isEmpty()) {
            return;
        }
        int sort = 0;
        for (Integer typeId : resourceTypeIds) {
            if (typeId == null) {
                continue;
            }
            EduResourceType type = requireResourceType(typeId);
            if (type.getParentId() == null || type.getParentId() <= 0) {
                throw new BusinessException(400, "资源类型绑定仅支持叶子节点：" + type.getName());
            }
            eduSubjectResourceTypeMapper.insertLink(subjectId, typeId, ++sort, 1);
        }
    }

    private void syncModuleStages(Integer moduleId, List<Integer> stageIds) {
        eduModuleStageMapper.deleteByModuleId(moduleId);
        if (stageIds == null || stageIds.isEmpty()) {
            return;
        }
        int sort = 0;
        for (Integer stageId : stageIds) {
            if (stageId == null) {
                continue;
            }
            requireStage(stageId);
            eduModuleStageMapper.insertLink(moduleId, stageId, ++sort);
        }
    }

    private void assertUniqueStageCode(String code, Integer excludeId) {
        if (!StringUtils.hasText(code)) {
            return;
        }
        LambdaQueryWrapper<EduStage> wrapper = new LambdaQueryWrapper<EduStage>()
                .eq(EduStage::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduStage::getId, excludeId);
        }
        Long count = eduStageMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new BusinessException(400, "学段编码已存在");
        }
    }

    private void applyStage(EduStage stage, AdminTaxonomyStageWriteDTO dto) {
        stage.setCode(dto.getCode().trim());
        stage.setName(dto.getName().trim());
        stage.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : null);
        stage.setSort(dto.getSort() != null ? dto.getSort() : 0);
        stage.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applySubject(EduSubject subject, AdminTaxonomySubjectWriteDTO dto) {
        subject.setStageId(dto.getStageId());
        subject.setCode(dto.getCode().trim());
        subject.setName(dto.getName().trim());
        subject.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : null);
        subject.setSort(dto.getSort() != null ? dto.getSort() : 0);
        subject.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyEdition(EduEdition edition, AdminTaxonomyEditionWriteDTO dto) {
        edition.setCode(dto.getCode().trim());
        edition.setName(dto.getName().trim());
        edition.setShortName(StringUtils.hasText(dto.getShortName()) ? dto.getShortName().trim() : null);
        edition.setStageId(dto.getStageId());
        edition.setSubjectId(dto.getSubjectId());
        edition.setPublisher(StringUtils.hasText(dto.getPublisher()) ? dto.getPublisher().trim() : null);
        edition.setYearLabel(StringUtils.hasText(dto.getYearLabel()) ? dto.getYearLabel().trim() : null);
        edition.setSort(dto.getSort() != null ? dto.getSort() : 0);
        edition.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyGrade(EduGrade grade, AdminTaxonomyGradeWriteDTO dto) {
        grade.setStageId(dto.getStageId());
        grade.setCode(StringUtils.hasText(dto.getCode()) ? dto.getCode().trim() : null);
        grade.setName(dto.getName().trim());
        grade.setSort(dto.getSort() != null ? dto.getSort() : 0);
        grade.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyVolume(EduVolume volume, AdminTaxonomyVolumeWriteDTO dto) {
        volume.setCode(dto.getCode().trim());
        volume.setName(dto.getName().trim());
        volume.setSort(dto.getSort() != null ? dto.getSort() : 0);
        volume.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
        volume.setStageId(dto.getStageId());
        volume.setSubjectId(dto.getSubjectId());
        volume.setEditionId(dto.getEditionId());
    }

    private void applyModule(EduModule module, AdminTaxonomyModuleWriteDTO dto) {
        module.setCode(dto.getCode().trim());
        module.setName(dto.getName().trim());
        module.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : null);
        module.setModuleCategory(StringUtils.hasText(dto.getModuleCategory()) ? dto.getModuleCategory().trim() : "sync");
        module.setDescription(StringUtils.hasText(dto.getDescription()) ? dto.getDescription().trim() : null);
        module.setSort(dto.getSort() != null ? dto.getSort() : 0);
        module.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyResourceType(EduResourceType type, AdminTaxonomyResourceTypeWriteDTO dto) {
        type.setParentId(dto.getParentId());
        type.setCode(dto.getCode().trim());
        type.setName(dto.getName().trim());
        type.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : null);
        type.setGroupCode(StringUtils.hasText(dto.getGroupCode()) ? dto.getGroupCode().trim() : null);
        type.setGroupName(StringUtils.hasText(dto.getGroupName()) ? dto.getGroupName().trim() : null);
        type.setAllowPreview(dto.getAllowPreview() != null ? dto.getAllowPreview() : 1);
        type.setSort(dto.getSort() != null ? dto.getSort() : 0);
        type.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private int normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }

    private void notifySearchSync() {
        taxonomySearchSyncHook.afterTaxonomyChanged();
    }
}
