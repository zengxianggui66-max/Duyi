package com.k12.resource.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.dto.CatalogBreadcrumbItemVO;
import com.k12.common.dto.CatalogBrowseContext;
import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.CatalogSchemeVO;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.common.entity.EduCatalogNode;
import com.k12.common.entity.EduCatalogScheme;
import com.k12.common.entity.EduResourceBrand;
import com.k12.resource.mapper.EduCatalogNodeMapper;
import com.k12.resource.mapper.EduCatalogSchemeMapper;
import com.k12.resource.mapper.EduResourceBrandMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CatalogService {

    private final EduCatalogSchemeMapper eduCatalogSchemeMapper;
    private final EduCatalogNodeMapper eduCatalogNodeMapper;
    private final EduResourceBrandMapper eduResourceBrandMapper;
    private final UnitCatalogStaticLoader unitCatalogStaticLoader;
    private final ObjectMapper objectMapper;
    public CatalogService(EduCatalogSchemeMapper eduCatalogSchemeMapper, EduCatalogNodeMapper eduCatalogNodeMapper, EduResourceBrandMapper eduResourceBrandMapper, UnitCatalogStaticLoader unitCatalogStaticLoader, ObjectMapper objectMapper) {
        this.eduCatalogSchemeMapper = eduCatalogSchemeMapper;
        this.eduCatalogNodeMapper = eduCatalogNodeMapper;
        this.eduResourceBrandMapper = eduResourceBrandMapper;
        this.unitCatalogStaticLoader = unitCatalogStaticLoader;
        this.objectMapper = objectMapper;
    }


    public List<CatalogSchemeVO> listSchemes(String brandCode, String stage, String subject) {
        List<EduCatalogScheme> schemes = eduCatalogSchemeMapper.listActiveByBrand(brandCode);
        Map<Long, String> brandCodeById = loadBrandCodeMap(schemes);
        return schemes.stream()
                .map(s -> CatalogSchemeVO.fromEntity(s, brandCodeById.get(s.getBrandId())))
                .collect(Collectors.toList());
    }

    public List<CatalogNodeVO> getTree(
            Integer schemeId,
            String schemeCode,
            String volumeKey,
            String gradeName,
            String edition,
            String subject) {

        EduCatalogScheme scheme = resolveScheme(schemeId, schemeCode);
        if (scheme == null) {
            return List.of();
        }

        List<EduCatalogNode> filtered = loadFilteredNodes(scheme.getId(), volumeKey, gradeName, subject, false);
        if (!filtered.isEmpty()) {
            return buildTree(filtered);
        }

        if ("textbook_unit".equals(scheme.getCode())) {
            return convertLegacyUnitTree(volumeKey, gradeName, edition, subject);
        }

        return List.of();
    }

    /**
     * 管理端目录树（可含禁用节点）
     */
    public List<CatalogNodeVO> getTreeForAdmin(
            Integer schemeId,
            String schemeCode,
            String volumeKey,
            String gradeName,
            String subject,
            boolean includeDisabled) {
        EduCatalogScheme scheme = resolveScheme(schemeId, schemeCode);
        if (scheme == null) {
            return List.of();
        }
        List<EduCatalogNode> filtered = loadFilteredNodes(
                scheme.getId(), volumeKey, gradeName, subject, includeDisabled);
        if (filtered.isEmpty()) {
            return List.of();
        }
        return buildTree(filtered);
    }

    /**
     * 仅从 DB 读取目录树（不回落 unit-catalog.json）
     */
    public List<CatalogNodeVO> getTreeFromDbOnly(
            Integer schemeId,
            String schemeCode,
            String volumeKey,
            String gradeName,
            String edition,
            String subject) {
        EduCatalogScheme scheme = resolveScheme(schemeId, schemeCode);
        if (scheme == null) {
            return List.of();
        }
        List<EduCatalogNode> filtered = loadFilteredNodes(scheme.getId(), volumeKey, gradeName, subject, false);
        if (filtered.isEmpty()) {
            return List.of();
        }
        return buildTree(filtered);
    }

    private List<EduCatalogNode> loadFilteredNodes(
            Integer schemeId,
            String volumeKey,
            String gradeName,
            String subject,
            boolean includeDisabled) {
        List<EduCatalogNode> flat = includeDisabled
                ? eduCatalogNodeMapper.listAllBySchemeId(schemeId, true)
                : eduCatalogNodeMapper.listBySchemeId(schemeId);
        List<EduCatalogNode> filtered = filterByVolume(flat, volumeKey, gradeName);
        return filterBySubject(filtered, subject);
    }

    public List<CatalogBreadcrumbItemVO> getBreadcrumb(Long nodeId) {
        if (nodeId == null || nodeId <= 0) {
            return List.of();
        }
        EduCatalogNode node = eduCatalogNodeMapper.findActiveById(nodeId);
        if (node == null) {
            return List.of();
        }

        List<EduCatalogNode> all = eduCatalogNodeMapper.listBySchemeId(node.getSchemeId());
        Map<Long, EduCatalogNode> byId = all.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));

        LinkedList<CatalogBreadcrumbItemVO> chain = new LinkedList<>();
        EduCatalogNode current = node;
        int guard = 0;
        while (current != null && guard++ < 32) {
            CatalogBreadcrumbItemVO item = new CatalogBreadcrumbItemVO();
            item.setId(current.getId());
            item.setName(current.getName());
            item.setCode(current.getCode());
            item.setNodeType(current.getNodeType());
            chain.addFirst(item);
            if (current.getParentId() == null || current.getParentId() <= 0) {
                break;
            }
            current = byId.get(current.getParentId());
        }
        return new ArrayList<>(chain);
    }

    public EduCatalogScheme resolveScheme(Integer schemeId, String schemeCode) {
        if (schemeId != null) {
            EduCatalogScheme scheme = eduCatalogSchemeMapper.selectById(schemeId);
            if (scheme != null && scheme.getStatus() != null && scheme.getStatus() == 1) {
                return scheme;
            }
        }
        if (StringUtils.hasText(schemeCode)) {
            return eduCatalogSchemeMapper.findByCode(schemeCode.trim());
        }
        return null;
    }

    public EduCatalogScheme resolveDefaultSchemeByBrand(String brandCode) {
        if (!StringUtils.hasText(brandCode)) {
            return eduCatalogSchemeMapper.findByCode("textbook_unit");
        }
        List<EduCatalogScheme> list = eduCatalogSchemeMapper.listActiveByBrand(brandCode);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 按目录节点类型解析是否包含子树（服务端权威口径）
     */
    public boolean resolveIncludeSubtreeForNode(Long catalogNodeId, Boolean clientHint) {
        if (catalogNodeId == null || catalogNodeId <= 0) {
            return clientHint == null || Boolean.TRUE.equals(clientHint);
        }
        EduCatalogNode node = eduCatalogNodeMapper.findActiveById(catalogNodeId);
        if (node == null) {
            return clientHint == null || Boolean.TRUE.equals(clientHint);
        }
        return com.k12.common.util.CatalogBrowseScope.resolveIncludeSubtree(
                node.getNodeType(), node.getDepth(), node.getCode(), clientHint);
    }

    /**
     * 解析目录节点浏览上下文（子树 ID、路径前缀、单元/课文名）
     */
    public CatalogBrowseContext resolveBrowseContext(Long catalogNodeId, boolean includeSubtree) {
        CatalogBrowseContext ctx = new CatalogBrowseContext();
        if (catalogNodeId == null || catalogNodeId <= 0) {
            return ctx;
        }
        EduCatalogNode node = eduCatalogNodeMapper.findActiveById(catalogNodeId);
        if (node == null) {
            return ctx;
        }

        ctx.setCatalogNodeId(catalogNodeId);
        ctx.setNodeType(node.getNodeType());
        ctx.setNamePath(node.getNamePath());
        ctx.setCatalogPathPrefix(node.getNamePath());
        ctx.setMeta(parseMeta(node.getMeta()));
        applyMetaDefaults(ctx);

        List<EduCatalogNode> all = eduCatalogNodeMapper.listBySchemeId(node.getSchemeId());
        Map<Long, List<EduCatalogNode>> childrenMap = buildChildrenMap(all);

        List<Long> nodeIds = new ArrayList<>();
        collectNodeIdsRecursive(node, includeSubtree, childrenMap, nodeIds);
        ctx.setNodeIds(nodeIds);

        Map<Long, EduCatalogNode> byId = all.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));

        for (Long id : nodeIds) {
            EduCatalogNode n = byId.get(id);
            if (n == null) {
                continue;
            }
            if ("unit".equals(n.getNodeType()) && !ctx.getUnitNames().contains(n.getName())) {
                ctx.getUnitNames().add(n.getName());
            }
            if ("lesson".equals(n.getNodeType()) && !ctx.getLessonNames().contains(n.getName())) {
                ctx.getLessonNames().add(n.getName());
            }
        }

        if ("lesson".equals(node.getNodeType())) {
            ctx.getLessonNames().clear();
            ctx.getLessonNames().add(node.getName());
            EduCatalogNode parent = byId.get(node.getParentId());
            if (parent != null && "unit".equals(parent.getNodeType())) {
                ctx.getUnitNames().clear();
                ctx.getUnitNames().add(parent.getName());
            }
        } else if ("unit".equals(node.getNodeType()) && !includeSubtree) {
            ctx.getLessonNames().clear();
        } else if ("leaf".equals(node.getNodeType())) {
            applyReviewLeafFallback(ctx, node, byId);
        }

        expandBrowseNameVariants(ctx);
        return ctx;
    }

    /** 单元/课文名别名扩展（OSS · vs 统编 ：、课文「1 天地人」vs「天地人」） */
    private void expandBrowseNameVariants(CatalogBrowseContext ctx) {
        expandUnitNameVariants(ctx.getUnitNames());
        expandLessonNameVariants(ctx.getLessonNames());
    }

    private void expandUnitNameVariants(List<String> unitNames) {
        if (unitNames == null || unitNames.isEmpty()) {
            return;
        }
        List<String> extras = new ArrayList<>();
        for (String u : unitNames) {
            if (!StringUtils.hasText(u)) {
                continue;
            }
            if (u.contains("：")) {
                extras.add(u.replace('：', '·'));
            }
            if (u.contains("·")) {
                extras.add(u.replace('·', '：'));
            }
        }
        for (String e : extras) {
            if (!unitNames.contains(e)) {
                unitNames.add(e);
            }
        }
    }

    private void expandLessonNameVariants(List<String> lessonNames) {
        if (lessonNames == null || lessonNames.isEmpty()) {
            return;
        }
        List<String> extras = new ArrayList<>();
        for (String l : lessonNames) {
            if (!StringUtils.hasText(l)) {
                continue;
            }
            if (l.matches("^\\d+\\s+\\S.+")) {
                extras.add(l.replaceFirst("^\\d+\\s+", ""));
            }
        }
        for (String e : extras) {
            if (!lessonNames.contains(e)) {
                lessonNames.add(e);
            }
        }
    }

    /**
     * 期末复习叶子节点回退：OSS 文件常挂在父文件夹，按 reviewScope + 标题关键词匹配。
     */
    private void applyReviewLeafFallback(
            CatalogBrowseContext ctx,
            EduCatalogNode node,
            Map<Long, EduCatalogNode> byId) {
        Map<String, Object> meta = ctx.getMeta();
        if (meta == null) {
            return;
        }
        String reviewScope = String.valueOf(meta.get("reviewScope"));
        EduCatalogNode parent = byId.get(node.getParentId());
        if (parent == null) {
            return;
        }

        if ("unit".equals(reviewScope) && meta.get("canonicalUnit") != null) {
            String unitName = String.valueOf(meta.get("canonicalUnit")).trim();
            if (!StringUtils.hasText(unitName)) {
                return;
            }
            if (!ctx.getUnitNames().contains(unitName)) {
                ctx.getUnitNames().add(unitName);
            }
            ctx.setFallbackParentNodeId(parent.getId());
            ctx.setTitleKeywords(buildReviewUnitTitleKeywords(unitName));
            return;
        }

        if ("special".equals(reviewScope) && meta.get("knowledgePoint") != null) {
            String knowledgePoint = String.valueOf(meta.get("knowledgePoint")).trim();
            if (!StringUtils.hasText(knowledgePoint)) {
                return;
            }
            ctx.setFallbackParentNodeId(parent.getId());
            ctx.setTitleKeywords(buildReviewSpecialTitleKeywords(knowledgePoint));
        }
    }

    private List<String> buildReviewUnitTitleKeywords(String canonicalUnit) {
        List<String> keys = new ArrayList<>();
        if (!StringUtils.hasText(canonicalUnit)) {
            return keys;
        }
        keys.add(canonicalUnit);
        int colon = canonicalUnit.indexOf('：');
        if (colon > 0) {
            keys.add(canonicalUnit.substring(0, colon));
        }
        if ("汉语拼音".equals(canonicalUnit)) {
            keys.add("第二、三单元");
            keys.add("第二，三单元");
        }
        if ("我上学了".equals(canonicalUnit)) {
            keys.add("我上学了");
        }
        return keys.stream().distinct().filter(StringUtils::hasText).collect(Collectors.toList());
    }

    /** 专项复习课件文件名（专项1：汉语拼音…）→ 目录知识点 */
    private List<String> buildReviewSpecialTitleKeywords(String knowledgePoint) {
        List<String> keys = new ArrayList<>();
        if (!StringUtils.hasText(knowledgePoint)) {
            return keys;
        }
        switch (knowledgePoint) {
            case "拼音与识字" -> {
                keys.add("汉语拼音");
                keys.add("生字");
                keys.add("拼音");
            }
            case "词语与句子" -> {
                keys.add("词语");
                keys.add("句子");
                keys.add("标点");
            }
            case "阅读与鉴赏" -> keys.add("阅读");
            case "口语交际" -> keys.add("口语交际");
            case "习作" -> {
                keys.add("写话");
                keys.add("看图");
            }
            case "古诗与积累" -> {
                keys.add("积累");
                keys.add("背诵");
                keys.add("古诗");
            }
            default -> keys.add(knowledgePoint);
        }
        return keys.stream().distinct().filter(StringUtils::hasText).collect(Collectors.toList());
    }

    private void applyMetaDefaults(CatalogBrowseContext ctx) {
        Map<String, Object> meta = ctx.getMeta();
        if (meta == null) {
            return;
        }
        if (meta.get("defaultModule") != null) {
            ctx.setDefaultModule(String.valueOf(meta.get("defaultModule")));
        }
        if (meta.get("defaultType") != null) {
            ctx.setDefaultType(String.valueOf(meta.get("defaultType")));
        }
        if (meta.get("subType") != null) {
            ctx.setSubType(String.valueOf(meta.get("subType")));
        }
    }

    private Map<Long, List<EduCatalogNode>> buildChildrenMap(List<EduCatalogNode> all) {
        Map<Long, List<EduCatalogNode>> map = new HashMap<>();
        for (EduCatalogNode n : all) {
            Long pid = n.getParentId() != null ? n.getParentId() : 0L;
            map.computeIfAbsent(pid, k -> new ArrayList<>()).add(n);
        }
        return map;
    }

    private void collectNodeIdsRecursive(
            EduCatalogNode node,
            boolean includeSubtree,
            Map<Long, List<EduCatalogNode>> childrenMap,
            List<Long> out) {
        if (node == null) {
            return;
        }
        out.add(node.getId());
        if (!includeSubtree) {
            return;
        }
        List<EduCatalogNode> children = childrenMap.getOrDefault(node.getId(), List.of());
        for (EduCatalogNode child : children) {
            collectNodeIdsRecursive(child, true, childrenMap, out);
        }
    }

    private Map<Long, String> loadBrandCodeMap(List<EduCatalogScheme> schemes) {
        Set<Long> brandIds = schemes.stream()
                .map(EduCatalogScheme::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (brandIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> map = new HashMap<>();
        for (EduResourceBrand brand : eduResourceBrandMapper.selectCodeByIds(brandIds)) {
            if (brand != null) {
                map.put(brand.getId(), brand.getCode());
            }
        }
        return map;
    }

    private List<EduCatalogNode> filterByVolume(
            List<EduCatalogNode> nodes,
            String volumeKey,
            String gradeName) {
        if (!StringUtils.hasText(volumeKey) && !StringUtils.hasText(gradeName)) {
            return nodes;
        }
        String key = resolveVolumeKey(volumeKey, gradeName);
        if (!StringUtils.hasText(key)) {
            return nodes;
        }

        Set<Long> keepIds = new HashSet<>();
        Map<Long, EduCatalogNode> byId = nodes.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));

        for (EduCatalogNode node : nodes) {
            String nodeVolume = readMetaString(node.getMeta(), "volumeKey");
            if (!StringUtils.hasText(nodeVolume) || key.equals(nodeVolume)) {
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
            boolean anyVolumeMeta = nodes.stream()
                    .anyMatch(n -> StringUtils.hasText(readMetaString(n.getMeta(), "volumeKey")));
            if (!anyVolumeMeta) {
                return nodes;
            }
            return List.of();
        }

        return nodes.stream()
                .filter(n -> keepIds.contains(n.getId()))
                .collect(Collectors.toList());
    }

    /** 按学科 meta.subject 过滤（与 volumeKey 联用：册别 + 语文/数学/英语） */
    private List<EduCatalogNode> filterBySubject(List<EduCatalogNode> nodes, String subject) {
        if (!StringUtils.hasText(subject)) {
            return nodes;
        }
        String key = subject.trim();
        Set<Long> keepIds = new HashSet<>();
        Map<Long, EduCatalogNode> byId = nodes.stream()
                .collect(Collectors.toMap(EduCatalogNode::getId, n -> n, (a, b) -> a));

        for (EduCatalogNode node : nodes) {
            String nodeSubject = readMetaString(node.getMeta(), "subject");
            if (!StringUtils.hasText(nodeSubject) || key.equals(nodeSubject)) {
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
            boolean anySubjectMeta = nodes.stream()
                    .anyMatch(n -> StringUtils.hasText(readMetaString(n.getMeta(), "subject")));
            if (!anySubjectMeta) {
                return nodes;
            }
            return List.of();
        }

        return nodes.stream()
                .filter(n -> keepIds.contains(n.getId()))
                .collect(Collectors.toList());
    }

    private List<CatalogNodeVO> buildTree(List<EduCatalogNode> nodes) {
        Map<Long, CatalogNodeVO> voMap = new LinkedHashMap<>();
        for (EduCatalogNode node : nodes) {
            voMap.put(node.getId(), toVo(node));
        }

        List<CatalogNodeVO> roots = new ArrayList<>();
        for (EduCatalogNode node : nodes) {
            CatalogNodeVO vo = voMap.get(node.getId());
            Long parentId = node.getParentId();
            if (parentId == null || parentId <= 0) {
                roots.add(vo);
            } else {
                CatalogNodeVO parent = voMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }

        for (CatalogNodeVO root : roots) {
            fillSubUnits(root);
        }
        return roots;
    }

    private void fillSubUnits(CatalogNodeVO node) {
        if (node.getChildren() == null || node.getChildren().isEmpty()) {
            return;
        }
        List<String> lessonNames = new ArrayList<>();
        for (CatalogNodeVO child : node.getChildren()) {
            if ("lesson".equals(child.getNodeType())) {
                lessonNames.add(child.getName());
            }
            fillSubUnits(child);
        }
        if (!lessonNames.isEmpty()) {
            node.setSubUnits(lessonNames);
        }
    }

    private CatalogNodeVO toVo(EduCatalogNode node) {
        CatalogNodeVO vo = new CatalogNodeVO();
        vo.setId(node.getId());
        vo.setCode(node.getCode());
        vo.setName(node.getName());
        vo.setNamePath(node.getNamePath());
        vo.setDepth(node.getDepth());
        vo.setNodeType(node.getNodeType());
        vo.setIcon(node.getIcon());
        vo.setMeta(parseMeta(node.getMeta()));
        return vo;
    }

    private Map<String, Object> parseMeta(String metaJson) {
        if (!StringUtils.hasText(metaJson)) {
            return null;
        }
        try {
            return objectMapper.readValue(metaJson, new TypeReference<>() {});
        } catch (Exception e) {
            log.debug("解析 catalog meta 失败: {}", metaJson);
            return null;
        }
    }

    private String readMetaString(String metaJson, String key) {
        Map<String, Object> meta = parseMeta(metaJson);
        if (meta == null || !meta.containsKey(key)) {
            return null;
        }
        Object val = meta.get(key);
        return val == null ? null : String.valueOf(val);
    }

    private List<CatalogNodeVO> convertLegacyUnitTree(
            String volumeKey,
            String gradeName,
            String edition,
            String subject) {
        List<UnitTreeNodeVO> legacy = unitCatalogStaticLoader.getTree(volumeKey, gradeName);
        List<CatalogNodeVO> result = new ArrayList<>();
        long syntheticId = -1L;
        for (UnitTreeNodeVO unit : legacy) {
            CatalogNodeVO unitVo = new CatalogNodeVO();
            unitVo.setId(syntheticId--);
            unitVo.setCode("legacy_unit");
            unitVo.setName(unit.getName());
            unitVo.setNamePath("/" + unit.getName());
            unitVo.setDepth(0);
            unitVo.setNodeType("unit");
            unitVo.setSubUnits(unit.getSubUnits() != null ? new ArrayList<>(unit.getSubUnits()) : new ArrayList<>());

            if (unit.getSubUnits() != null) {
                for (String lesson : unit.getSubUnits()) {
                    CatalogNodeVO lessonVo = new CatalogNodeVO();
                    lessonVo.setId(syntheticId--);
                    lessonVo.setCode("legacy_lesson");
                    lessonVo.setName(lesson);
                    lessonVo.setNamePath("/" + unit.getName() + "/" + lesson);
                    lessonVo.setDepth(1);
                    lessonVo.setNodeType("lesson");
                    unitVo.getChildren().add(lessonVo);
                }
            }
            result.add(unitVo);
        }
        return result;
    }

    private String resolveVolumeKey(String volumeKey, String gradeName) {
        if (StringUtils.hasText(volumeKey)) {
            return volumeKey.trim();
        }
        if (!StringUtils.hasText(gradeName)) {
            return "";
        }
        if (gradeName.contains("一年级") && gradeName.contains("下册")) return "y1s2";
        if (gradeName.contains("一年级") && gradeName.contains("上册")) return "y1s1";
        if (gradeName.contains("二年级") && gradeName.contains("下册")) return "y2s2";
        if (gradeName.contains("二年级") && gradeName.contains("上册")) return "y2s1";
        if (gradeName.contains("三年级") && gradeName.contains("下册")) return "y3s2";
        if (gradeName.contains("三年级") && gradeName.contains("上册")) return "y3s1";
        if (gradeName.contains("七年级") && gradeName.contains("下册")) return "j7s2";
        if (gradeName.contains("七年级") && gradeName.contains("上册")) return "j7s1";
        if (gradeName.contains("高一") && gradeName.contains("下册")) return "s10s2";
        if (gradeName.contains("高一") && gradeName.contains("上册")) return "s10s1";
        return "";
    }
}
