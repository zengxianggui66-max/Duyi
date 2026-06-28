package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.*;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.*;
import com.k12.resource.service.AdminDictionaryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@SuppressWarnings("null")
public class AdminDictionaryServiceImpl implements AdminDictionaryService {

    private static final String PERM_EDIT = "admin:taxonomy:edit";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final EduExamSceneMapper eduExamSceneMapper;
    private final EduFileFormatMapper eduFileFormatMapper;
    private final EduRegionMapper eduRegionMapper;
    private final EduTeachingSceneMapper eduTeachingSceneMapper;
    private final EduBrowseTagMapper eduBrowseTagMapper;
    private final AdminPermissionService adminPermissionService;
    public AdminDictionaryServiceImpl(EduExamSceneMapper eduExamSceneMapper, EduFileFormatMapper eduFileFormatMapper, EduRegionMapper eduRegionMapper, EduTeachingSceneMapper eduTeachingSceneMapper, EduBrowseTagMapper eduBrowseTagMapper, AdminPermissionService adminPermissionService) {
        this.eduExamSceneMapper = eduExamSceneMapper;
        this.eduFileFormatMapper = eduFileFormatMapper;
        this.eduRegionMapper = eduRegionMapper;
        this.eduTeachingSceneMapper = eduTeachingSceneMapper;
        this.eduBrowseTagMapper = eduBrowseTagMapper;
        this.adminPermissionService = adminPermissionService;
    }


    // ==================== 考试场景 ====================

    @Override
    public List<EduExamScene> listExamScenes(boolean includeDisabled) {
        LambdaQueryWrapper<EduExamScene> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduExamScene::getStatus, 1);
        }
        wrapper.orderByAsc(EduExamScene::getSort).orderByAsc(EduExamScene::getId);
        return eduExamSceneMapper.selectList(wrapper);
    }

    @Override
    public EduExamScene createExamScene(AdminDictionaryExamSceneWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueExamSceneCode(dto.getCode(), null);
        EduExamScene row = new EduExamScene();
        applyExamScene(row, dto);
        eduExamSceneMapper.insert(row);
        return row;
    }

    @Override
    public EduExamScene updateExamScene(Integer id, AdminDictionaryExamSceneWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduExamScene row = requireExamScene(id);
        assertUniqueExamSceneCode(dto.getCode(), id);
        applyExamScene(row, dto);
        eduExamSceneMapper.updateById(row);
        return row;
    }

    @Override
    public void setExamSceneStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduExamScene row = requireExamScene(id);
        row.setStatus(normalizeStatus(status));
        eduExamSceneMapper.updateById(row);
    }

    @Override
    public void deleteExamScene(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireExamScene(id);
        eduExamSceneMapper.deleteById(id);
    }

    // ==================== 文件格式 ====================

    @Override
    public List<EduFileFormat> listFileFormats(boolean includeDisabled) {
        LambdaQueryWrapper<EduFileFormat> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduFileFormat::getStatus, 1);
        }
        wrapper.orderByAsc(EduFileFormat::getSort).orderByAsc(EduFileFormat::getId);
        return eduFileFormatMapper.selectList(wrapper);
    }

    @Override
    public EduFileFormat createFileFormat(AdminDictionaryFileFormatWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueFileFormatCode(dto.getCode(), null);
        EduFileFormat row = new EduFileFormat();
        applyFileFormat(row, dto);
        eduFileFormatMapper.insert(row);
        return row;
    }

    @Override
    public EduFileFormat updateFileFormat(Integer id, AdminDictionaryFileFormatWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduFileFormat row = requireFileFormat(id);
        assertUniqueFileFormatCode(dto.getCode(), id);
        applyFileFormat(row, dto);
        eduFileFormatMapper.updateById(row);
        return row;
    }

    @Override
    public void setFileFormatStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduFileFormat row = requireFileFormat(id);
        row.setStatus(normalizeStatus(status));
        eduFileFormatMapper.updateById(row);
    }

    @Override
    public void deleteFileFormat(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireFileFormat(id);
        eduFileFormatMapper.deleteById(id);
    }

    // ==================== 地区 ====================

    @Override
    public List<EduRegion> listRegions(Integer parentId, boolean includeDisabled) {
        LambdaQueryWrapper<EduRegion> wrapper = new LambdaQueryWrapper<>();
        if (parentId != null) {
            wrapper.eq(EduRegion::getParentId, parentId);
        }
        if (!includeDisabled) {
            wrapper.eq(EduRegion::getStatus, 1);
        }
        wrapper.orderByAsc(EduRegion::getSort).orderByAsc(EduRegion::getId);
        return eduRegionMapper.selectList(wrapper);
    }

    @Override
    public EduRegion createRegion(AdminDictionaryRegionWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueRegionCode(dto.getCode(), null);
        if (dto.getParentId() > 0) {
            requireRegion(dto.getParentId());
        }
        EduRegion row = new EduRegion();
        applyRegion(row, dto);
        eduRegionMapper.insert(row);
        return row;
    }

    @Override
    public EduRegion updateRegion(Integer id, AdminDictionaryRegionWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduRegion row = requireRegion(id);
        assertUniqueRegionCode(dto.getCode(), id);
        if (dto.getParentId() != null && dto.getParentId() > 0) {
            requireRegion(dto.getParentId());
        }
        if (id.equals(dto.getParentId())) {
            throw new BusinessException(400, "上级地区不能是自己");
        }
        applyRegion(row, dto);
        eduRegionMapper.updateById(row);
        return row;
    }

    @Override
    public void setRegionStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduRegion row = requireRegion(id);
        row.setStatus(normalizeStatus(status));
        eduRegionMapper.updateById(row);
    }

    @Override
    public void deleteRegion(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireRegion(id);
        Long childCount = eduRegionMapper.selectCount(new LambdaQueryWrapper<EduRegion>().eq(EduRegion::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException(400, "存在下级地区，无法删除");
        }
        eduRegionMapper.deleteById(id);
    }

    // ==================== 教学场景 ====================

    @Override
    public List<EduTeachingScene> listTeachingScenes(boolean includeDisabled) {
        LambdaQueryWrapper<EduTeachingScene> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduTeachingScene::getStatus, 1);
        }
        wrapper.orderByAsc(EduTeachingScene::getSort).orderByAsc(EduTeachingScene::getId);
        return eduTeachingSceneMapper.selectList(wrapper);
    }

    @Override
    public EduTeachingScene createTeachingScene(AdminDictionaryTeachingSceneWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueTeachingSceneCode(dto.getCode(), null);
        EduTeachingScene row = new EduTeachingScene();
        applyTeachingScene(row, dto);
        eduTeachingSceneMapper.insert(row);
        return row;
    }

    @Override
    public EduTeachingScene updateTeachingScene(Integer id, AdminDictionaryTeachingSceneWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduTeachingScene row = requireTeachingScene(id);
        assertUniqueTeachingSceneCode(dto.getCode(), id);
        applyTeachingScene(row, dto);
        eduTeachingSceneMapper.updateById(row);
        return row;
    }

    @Override
    public void setTeachingSceneStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduTeachingScene row = requireTeachingScene(id);
        row.setStatus(normalizeStatus(status));
        eduTeachingSceneMapper.updateById(row);
    }

    @Override
    public void deleteTeachingScene(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireTeachingScene(id);
        eduTeachingSceneMapper.deleteById(id);
    }

    // ==================== 资源属性标签 ====================

    @Override
    public List<EduBrowseTag> listBrowseTags(boolean includeDisabled) {
        LambdaQueryWrapper<EduBrowseTag> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduBrowseTag::getStatus, 1);
        }
        wrapper.orderByAsc(EduBrowseTag::getSort).orderByAsc(EduBrowseTag::getId);
        return eduBrowseTagMapper.selectList(wrapper);
    }

    @Override
    public EduBrowseTag createBrowseTag(AdminDictionaryBrowseTagWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        assertUniqueBrowseTagCode(dto.getCode(), null);
        EduBrowseTag row = new EduBrowseTag();
        applyBrowseTag(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        eduBrowseTagMapper.insert(row);
        return row;
    }

    @Override
    public EduBrowseTag updateBrowseTag(Integer id, AdminDictionaryBrowseTagWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduBrowseTag row = requireBrowseTag(id);
        assertUniqueBrowseTagCode(dto.getCode(), id);
        applyBrowseTag(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        eduBrowseTagMapper.updateById(row);
        return row;
    }

    @Override
    public void setBrowseTagStatus(Integer id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduBrowseTag row = requireBrowseTag(id);
        row.setStatus(normalizeStatus(status));
        row.setUpdateTime(LocalDateTime.now());
        eduBrowseTagMapper.updateById(row);
    }

    @Override
    public void deleteBrowseTag(Integer id, Long adminUserId) {
        requireEdit(adminUserId);
        requireBrowseTag(id);
        eduBrowseTagMapper.deleteById(id);
    }

    // ==================== helpers ====================

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无字典/标签编辑权限");
        }
    }

    private EduExamScene requireExamScene(Integer id) {
        EduExamScene row = eduExamSceneMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "考试场景不存在");
        }
        return row;
    }

    private EduFileFormat requireFileFormat(Integer id) {
        EduFileFormat row = eduFileFormatMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "文件格式不存在");
        }
        return row;
    }

    private EduRegion requireRegion(Integer id) {
        EduRegion row = eduRegionMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "地区不存在");
        }
        return row;
    }

    private EduTeachingScene requireTeachingScene(Integer id) {
        EduTeachingScene row = eduTeachingSceneMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "教学场景不存在");
        }
        return row;
    }

    private EduBrowseTag requireBrowseTag(Integer id) {
        EduBrowseTag row = eduBrowseTagMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "资源标签不存在");
        }
        return row;
    }

    private void assertUniqueExamSceneCode(String code, Integer excludeId) {
        LambdaQueryWrapper<EduExamScene> wrapper = new LambdaQueryWrapper<EduExamScene>()
                .eq(EduExamScene::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduExamScene::getId, excludeId);
        }
        if (eduExamSceneMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "考试场景编码已存在");
        }
    }

    private void assertUniqueFileFormatCode(String code, Integer excludeId) {
        LambdaQueryWrapper<EduFileFormat> wrapper = new LambdaQueryWrapper<EduFileFormat>()
                .eq(EduFileFormat::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduFileFormat::getId, excludeId);
        }
        if (eduFileFormatMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "文件格式编码已存在");
        }
    }

    private void assertUniqueRegionCode(String code, Integer excludeId) {
        LambdaQueryWrapper<EduRegion> wrapper = new LambdaQueryWrapper<EduRegion>()
                .eq(EduRegion::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduRegion::getId, excludeId);
        }
        if (eduRegionMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "地区编码已存在");
        }
    }

    private void assertUniqueTeachingSceneCode(String code, Integer excludeId) {
        LambdaQueryWrapper<EduTeachingScene> wrapper = new LambdaQueryWrapper<EduTeachingScene>()
                .eq(EduTeachingScene::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduTeachingScene::getId, excludeId);
        }
        if (eduTeachingSceneMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "教学场景编码已存在");
        }
    }

    private void assertUniqueBrowseTagCode(String code, Integer excludeId) {
        LambdaQueryWrapper<EduBrowseTag> wrapper = new LambdaQueryWrapper<EduBrowseTag>()
                .eq(EduBrowseTag::getCode, code.trim());
        if (excludeId != null) {
            wrapper.ne(EduBrowseTag::getId, excludeId);
        }
        if (eduBrowseTagMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "资源标签编码已存在");
        }
    }

    private void applyExamScene(EduExamScene row, AdminDictionaryExamSceneWriteDTO dto) {
        row.setCode(dto.getCode().trim());
        row.setName(dto.getName().trim());
        row.setExamLevel(StringUtils.hasText(dto.getExamLevel()) ? dto.getExamLevel().trim() : null);
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyFileFormat(EduFileFormat row, AdminDictionaryFileFormatWriteDTO dto) {
        row.setCode(dto.getCode().trim());
        row.setName(dto.getName().trim());
        row.setExtensions(dto.getExtensions().trim());
        row.setMimeTypes(StringUtils.hasText(dto.getMimeTypes()) ? dto.getMimeTypes().trim() : null);
        row.setPreviewType(StringUtils.hasText(dto.getPreviewType()) ? dto.getPreviewType().trim() : null);
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyRegion(EduRegion row, AdminDictionaryRegionWriteDTO dto) {
        row.setParentId(dto.getParentId() != null ? dto.getParentId() : 0);
        row.setCode(dto.getCode().trim());
        row.setName(dto.getName().trim());
        row.setLevel(dto.getLevel() != null ? dto.getLevel() : 1);
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyTeachingScene(EduTeachingScene row, AdminDictionaryTeachingSceneWriteDTO dto) {
        row.setCode(dto.getCode().trim());
        row.setName(dto.getName().trim());
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private void applyBrowseTag(EduBrowseTag row, AdminDictionaryBrowseTagWriteDTO dto) {
        row.setCode(dto.getCode().trim());
        row.setName(dto.getName().trim());
        row.setTagGroup(StringUtils.hasText(dto.getTagGroup()) ? dto.getTagGroup().trim() : "core");
        row.setApplicableStages(toJsonList(dto.getApplicableStages()));
        row.setApplicableModules(toJsonList(dto.getApplicableModules()));
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? normalizeStatus(dto.getStatus()) : 1);
    }

    private String toJsonList(List<String> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(list);
        } catch (Exception e) {
            throw new BusinessException(400, "适用范围 JSON 格式错误");
        }
    }

    private int normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }
}
