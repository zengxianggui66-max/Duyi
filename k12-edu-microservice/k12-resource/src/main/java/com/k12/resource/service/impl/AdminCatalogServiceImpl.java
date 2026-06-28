package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminCatalogNodeAdminVO;
import com.k12.common.dto.AdminCatalogNodeWriteDTO;
import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.CatalogSchemeVO;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.common.entity.EduCatalogNode;
import com.k12.common.entity.EduCatalogScheme;
import com.k12.common.entity.EduEdition;
import com.k12.common.entity.EduStage;
import com.k12.common.entity.EduSubject;
import com.k12.common.entity.EduVolume;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.EduCatalogNodeMapper;
import com.k12.resource.mapper.EduCatalogSchemeMapper;
import com.k12.resource.mapper.EduEditionMapper;
import com.k12.resource.mapper.EduStageMapper;
import com.k12.resource.mapper.EduSubjectMapper;
import com.k12.resource.mapper.EduVolumeMapper;
import com.k12.resource.service.AdminCatalogService;
import com.k12.resource.service.CatalogService;
import com.k12.resource.service.UnitCatalogService;
import com.k12.resource.search.TaxonomySearchSyncHook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class AdminCatalogServiceImpl implements AdminCatalogService {

    private static final String PERM_EDIT = "admin:taxonomy:edit";

    private final EduCatalogSchemeMapper eduCatalogSchemeMapper;
    private final EduCatalogNodeMapper eduCatalogNodeMapper;
    private final CatalogService catalogService;
    private final UnitCatalogService unitCatalogService;
    private final AdminPermissionService adminPermissionService;
    private final TaxonomySearchSyncHook taxonomySearchSyncHook;
    private final EduStageMapper eduStageMapper;
    private final EduSubjectMapper eduSubjectMapper;
    private final EduEditionMapper eduEditionMapper;
    private final EduVolumeMapper eduVolumeMapper;

    public AdminCatalogServiceImpl(
            EduCatalogSchemeMapper eduCatalogSchemeMapper,
            EduCatalogNodeMapper eduCatalogNodeMapper,
            CatalogService catalogService,
            UnitCatalogService unitCatalogService,
            AdminPermissionService adminPermissionService,
            TaxonomySearchSyncHook taxonomySearchSyncHook,
            EduStageMapper eduStageMapper,
            EduSubjectMapper eduSubjectMapper,
            EduEditionMapper eduEditionMapper,
            EduVolumeMapper eduVolumeMapper) {
        this.eduCatalogSchemeMapper = eduCatalogSchemeMapper;
        this.eduCatalogNodeMapper = eduCatalogNodeMapper;
        this.catalogService = catalogService;
        this.unitCatalogService = unitCatalogService;
        this.adminPermissionService = adminPermissionService;
        this.taxonomySearchSyncHook = taxonomySearchSyncHook;
        this.eduStageMapper = eduStageMapper;
        this.eduSubjectMapper = eduSubjectMapper;
        this.eduEditionMapper = eduEditionMapper;
        this.eduVolumeMapper = eduVolumeMapper;
    }

    @Override
    public List<CatalogSchemeVO> listSchemes() {
        List<EduCatalogScheme> schemes = eduCatalogSchemeMapper.selectList(null);
        Map<Long, String> brandCodes = loadBrandCodeMap(schemes);
        // Phase 6：预加载维度名称
        Map<Integer, String> stageNames = loadStageNameMap(schemes);
        Map<Integer, String> subjectNames = loadSubjectNameMap(schemes);
        Map<Integer, String> editionNames = loadEditionNameMap(schemes);
        Map<Integer, String> volumeNames = loadVolumeNameMap(schemes);
        return schemes.stream()
                .filter(s -> s.getStatus() == null || s.getStatus() == 1)
                .sorted(Comparator.comparing(EduCatalogScheme::getSort, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(EduCatalogScheme::getId))
                .map(s -> {
                    CatalogSchemeVO vo = CatalogSchemeVO.fromEntity(s, brandCodes.get(s.getBrandId()));
                    vo.setStageName(stageNames.get(s.getStageId()));
                    vo.setSubjectName(subjectNames.get(s.getSubjectId()));
                    vo.setEditionName(editionNames.get(s.getEditionId()));
                    vo.setVolumeName(volumeNames.get(s.getVolumeId()));
                    return vo;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<CatalogNodeVO> getAdminTree(
            Integer schemeId,
            String schemeCode,
            String volumeKey,
            String gradeName,
            String subject,
            boolean includeDisabled) {
        return catalogService.getTreeForAdmin(schemeId, schemeCode, volumeKey, gradeName, subject, includeDisabled);
    }

    @Override
    public List<AdminCatalogNodeAdminVO> listNodesFlat(
            Integer schemeId,
            Long parentId,
            String volumeKey,
            boolean includeDisabled) {
        if (schemeId == null) {
            return List.of();
        }
        EduCatalogScheme scheme = eduCatalogSchemeMapper.selectById(schemeId);
        List<EduCatalogNode> all = eduCatalogNodeMapper.listAllBySchemeId(schemeId, includeDisabled);
        Map<Long, EduCatalogNode> byId = all.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));

        List<EduCatalogNode> filtered = all;
        if (StringUtils.hasText(volumeKey)) {
            filtered = filterByVolumeKey(all, volumeKey.trim());
        }
        if (parentId != null) {
            filtered = filtered.stream()
                    .filter(n -> Objects.equals(n.getParentId(), parentId))
                    .collect(Collectors.toList());
        }

        List<AdminCatalogNodeAdminVO> result = new ArrayList<>(filtered.size());
        for (EduCatalogNode node : filtered) {
            String parentName = null;
            if (node.getParentId() != null && node.getParentId() > 0) {
                EduCatalogNode parent = byId.get(node.getParentId());
                parentName = parent != null ? parent.getName() : null;
            }
            long childCount = all.stream()
                    .filter(n -> Objects.equals(n.getParentId(), node.getId()))
                    .count();
            result.add(AdminCatalogNodeAdminVO.from(
                    node,
                    scheme != null ? scheme.getCode() : null,
                    scheme != null ? scheme.getName() : null,
                    parentName,
                    childCount));
        }
        return result;
    }

    @Override
    @Transactional
    public EduCatalogNode createNode(AdminCatalogNodeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduCatalogScheme scheme = requireScheme(dto.getSchemeId());
        EduCatalogNode node = new EduCatalogNode();
        applyNode(node, dto, scheme);
        eduCatalogNodeMapper.insert(node);
        taxonomySearchSyncHook.afterCatalogChanged();
        return node;
    }

    @Override
    @Transactional
    public EduCatalogNode updateNode(Long id, AdminCatalogNodeWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        EduCatalogNode node = requireNode(id);
        EduCatalogScheme scheme = requireScheme(dto.getSchemeId());
        applyNode(node, dto, scheme);
        eduCatalogNodeMapper.updateById(node);
        taxonomySearchSyncHook.afterCatalogChanged();
        return node;
    }

    @Override
    public void setNodeStatus(Long id, Integer status, Long adminUserId) {
        requireEdit(adminUserId);
        EduCatalogNode node = requireNode(id);
        node.setStatus(status != null && status == 0 ? 0 : 1);
        eduCatalogNodeMapper.updateById(node);
        taxonomySearchSyncHook.afterCatalogChanged();
    }

    @Override
    @Transactional
    public void deleteNode(Long id, Long adminUserId) {
        requireEdit(adminUserId);
        requireNode(id);
        if (eduCatalogNodeMapper.countChildren(id) > 0) {
            throw new BusinessException(400, "请先删除子节点");
        }
        // Phase 6：检查是否有资源挂载
        long refCount = eduCatalogNodeMapper.countDistinctResourcesByNodeIds(List.of(id));
        if (refCount > 0) {
            throw new BusinessException(400,
                    String.format("该节点已被 %d 个资源挂载，无法删除，建议改为禁用", refCount));
        }
        eduCatalogNodeMapper.deleteById(id);
        taxonomySearchSyncHook.afterCatalogChanged();
    }

    @Override
    @Transactional
    public int importVolumeFromStaticJson(String volumeKey, Long adminUserId) {
        requireEdit(adminUserId);
        if (!StringUtils.hasText(volumeKey)) {
            throw new BusinessException(400, "volumeKey 不能为空");
        }
        EduCatalogScheme scheme = eduCatalogSchemeMapper.findByCode("textbook_unit");
        if (scheme == null) {
            throw new BusinessException(404, "未找到 textbook_unit 目录方案");
        }
        List<UnitTreeNodeVO> units = unitCatalogService.getUnitTreeFromStaticJson(volumeKey.trim(), null, null, null);
        if (units.isEmpty()) {
            throw new BusinessException(404, "静态配置中无该册别: " + volumeKey);
        }

        String vk = volumeKey.trim();
        deleteVolumeNodes(scheme.getId(), vk);

        int count = 0;
        int unitSort = 0;
        for (UnitTreeNodeVO unit : units) {
            EduCatalogNode unitNode = buildNode(
                    scheme.getId(),
                    0L,
                    vk + "_unit_" + (++unitSort),
                    unit.getName(),
                    "unit",
                    unitSort,
                    metaJson(vk, null, null));
            eduCatalogNodeMapper.insert(unitNode);
            count++;

            int lessonSort = 0;
            if (unit.getSubUnits() != null) {
                for (String lesson : unit.getSubUnits()) {
                    if (!StringUtils.hasText(lesson)) {
                        continue;
                    }
                    EduCatalogNode lessonNode = buildNode(
                            scheme.getId(),
                            unitNode.getId(),
                            vk + "_unit_" + unitSort + "_lesson_" + (++lessonSort),
                            lesson.trim(),
                            "lesson",
                            lessonSort,
                            metaJson(vk, unit.getName(), null));
                    eduCatalogNodeMapper.insert(lessonNode);
                    count++;
                }
            }
        }
        taxonomySearchSyncHook.afterCatalogChanged();
        return count;
    }

    private void deleteVolumeNodes(Integer schemeId, String volumeKey) {
        List<EduCatalogNode> all = eduCatalogNodeMapper.listAllBySchemeId(schemeId, true);
        List<EduCatalogNode> matched = filterByVolumeKey(all, volumeKey);
        if (matched.isEmpty()) {
            return;
        }
        Set<Long> ids = matched.stream().map(EduCatalogNode::getId).collect(Collectors.toSet());
        List<Long> deleteOrder = matched.stream()
                .sorted(Comparator.comparing(EduCatalogNode::getDepth, Comparator.nullsLast(Integer::compareTo)).reversed())
                .map(EduCatalogNode::getId)
                .collect(Collectors.toList());
        for (Long id : deleteOrder) {
            if (ids.contains(id)) {
                eduCatalogNodeMapper.deleteById(id);
            }
        }
    }

    private EduCatalogNode buildNode(
            Integer schemeId,
            Long parentId,
            String code,
            String name,
            String nodeType,
            int sort,
            String meta) {
        EduCatalogNode node = new EduCatalogNode();
        node.setSchemeId(schemeId);
        node.setParentId(parentId != null ? parentId : 0L);
        node.setCode(code);
        node.setName(name);
        node.setNodeType(nodeType);
        node.setSort(sort);
        node.setMeta(meta);
        node.setStatus(1);
        fillPath(node, parentId);
        return node;
    }

    private String metaJson(String volumeKey, String unitName, String subject) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"volumeKey\":\"").append(volumeKey).append("\"");
        if (StringUtils.hasText(subject)) {
            sb.append(",\"subject\":\"").append(subject).append("\"");
        }
        if (StringUtils.hasText(unitName)) {
            sb.append(",\"unitName\":\"").append(unitName.replace("\"", "\\\"")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    private void applyNode(EduCatalogNode node, AdminCatalogNodeWriteDTO dto, EduCatalogScheme scheme) {
        node.setSchemeId(scheme.getId());
        Long parentId = dto.getParentId() != null ? dto.getParentId() : 0L;
        if (parentId > 0) {
            EduCatalogNode parent = requireNode(parentId);
            if (!Objects.equals(parent.getSchemeId(), scheme.getId())) {
                throw new BusinessException(400, "父节点与目录方案不一致");
            }
        }
        node.setParentId(parentId);
        node.setCode(dto.getCode().trim());
        node.setName(dto.getName().trim());
        node.setNodeType(dto.getNodeType().trim());
        node.setSort(dto.getSort() != null ? dto.getSort() : 0);
        node.setIcon(StringUtils.hasText(dto.getIcon()) ? dto.getIcon().trim() : null);
        node.setMeta(StringUtils.hasText(dto.getMeta()) ? dto.getMeta().trim() : null);
        node.setStatus(dto.getStatus() != null && dto.getStatus() == 0 ? 0 : 1);
        fillPath(node, parentId);
    }

    private void fillPath(EduCatalogNode node, Long parentId) {
        if (parentId == null || parentId <= 0) {
            node.setDepth(0);
            node.setNamePath("/" + node.getName());
            return;
        }
        EduCatalogNode parent = requireNode(parentId);
        node.setDepth((parent.getDepth() != null ? parent.getDepth() : 0) + 1);
        String parentPath = StringUtils.hasText(parent.getNamePath()) ? parent.getNamePath() : "/" + parent.getName();
        node.setNamePath(parentPath + "/" + node.getName());
    }

    private List<EduCatalogNode> filterByVolumeKey(List<EduCatalogNode> nodes, String volumeKey) {
        Set<Long> keepIds = new HashSet<>();
        Map<Long, EduCatalogNode> byId = nodes.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));
        for (EduCatalogNode node : nodes) {
            String vk = readMetaVolumeKey(node.getMeta());
            if (!StringUtils.hasText(vk) || volumeKey.equals(vk)) {
                keepIds.add(node.getId());
                Long pid = node.getParentId();
                int guard = 0;
                while (pid != null && pid > 0 && guard++ < 32) {
                    keepIds.add(pid);
                    EduCatalogNode parent = byId.get(pid);
                    if (parent == null) {
                        break;
                    }
                    pid = parent.getParentId();
                }
            }
        }
        if (keepIds.isEmpty()) {
            return List.of();
        }
        return nodes.stream().filter(n -> keepIds.contains(n.getId())).collect(Collectors.toList());
    }

    private String readMetaVolumeKey(String metaJson) {
        if (!StringUtils.hasText(metaJson)) {
            return null;
        }
        int idx = metaJson.indexOf("\"volumeKey\"");
        if (idx < 0) {
            return null;
        }
        int colon = metaJson.indexOf(':', idx);
        int q1 = metaJson.indexOf('"', colon + 1);
        int q2 = metaJson.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) {
            return null;
        }
        return metaJson.substring(q1 + 1, q2);
    }

    private EduCatalogScheme requireScheme(Integer id) {
        EduCatalogScheme scheme = eduCatalogSchemeMapper.selectById(id);
        if (scheme == null) {
            throw new BusinessException(404, "目录方案不存在");
        }
        return scheme;
    }

    private EduCatalogNode requireNode(Long id) {
        EduCatalogNode node = eduCatalogNodeMapper.findById(id);
        if (node == null) {
            throw new BusinessException(404, "目录节点不存在");
        }
        return node;
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无目录编辑权限");
        }
    }

    private Map<Long, String> loadBrandCodeMap(List<EduCatalogScheme> schemes) {
        Set<Long> brandIds = new HashSet<>();
        for (EduCatalogScheme scheme : schemes) {
            if (scheme.getBrandId() != null) {
                brandIds.add(scheme.getBrandId());
            }
        }
        if (brandIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new HashMap<>();
        List<Map<String, Object>> brandRows = eduCatalogSchemeMapper.selectBrandCodesByIds(brandIds);
        for (Map<String, Object> row : brandRows) {
            if (row == null || row.get("id") == null) {
                continue;
            }
            Long id = ((Number) row.get("id")).longValue();
            Object code = row.get("code");
            map.put(id, code != null ? String.valueOf(code) : null);
        }
        return map;
    }

    // ==================== Phase 6：维度名称预加载 ====================

    private Map<Integer, String> loadStageNameMap(List<EduCatalogScheme> schemes) {
        Set<Integer> ids = collectSchemeIntIds(schemes, EduCatalogScheme::getStageId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<EduStage> rows = eduStageMapper.selectList(new LambdaQueryWrapper<EduStage>().in(EduStage::getId, ids));
        return toNameMap(rows, EduStage::getId, EduStage::getName);
    }

    private Map<Integer, String> loadSubjectNameMap(List<EduCatalogScheme> schemes) {
        Set<Integer> ids = collectSchemeIntIds(schemes, EduCatalogScheme::getSubjectId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<EduSubject> rows = eduSubjectMapper.selectList(new LambdaQueryWrapper<EduSubject>().in(EduSubject::getId, ids));
        return toNameMap(rows, EduSubject::getId, EduSubject::getName);
    }

    private Map<Integer, String> loadEditionNameMap(List<EduCatalogScheme> schemes) {
        Set<Integer> ids = collectSchemeIntIds(schemes, EduCatalogScheme::getEditionId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<EduEdition> rows = eduEditionMapper.selectList(new LambdaQueryWrapper<EduEdition>().in(EduEdition::getId, ids));
        return toNameMap(rows, EduEdition::getId, EduEdition::getName);
    }

    private Map<Integer, String> loadVolumeNameMap(List<EduCatalogScheme> schemes) {
        Set<Integer> ids = collectSchemeIntIds(schemes, EduCatalogScheme::getVolumeId);
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<EduVolume> rows = eduVolumeMapper.selectList(new LambdaQueryWrapper<EduVolume>().in(EduVolume::getId, ids));
        return toNameMap(rows, EduVolume::getId, EduVolume::getName);
    }

    private Set<Integer> collectSchemeIntIds(List<EduCatalogScheme> schemes, Function<EduCatalogScheme, Integer> getter) {
        Set<Integer> ids = new HashSet<>();
        for (EduCatalogScheme scheme : schemes) {
            Integer id = getter.apply(scheme);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    private <T> Map<Integer, String> toNameMap(List<T> rows, Function<T, Integer> idGetter, Function<T, String> nameGetter) {
        Map<Integer, String> map = new HashMap<>();
        for (T row : rows) {
            if (row == null) {
                continue;
            }
            Integer id = idGetter.apply(row);
            if (id != null) {
                map.put(id, nameGetter.apply(row));
            }
        }
        return map;
    }
}
