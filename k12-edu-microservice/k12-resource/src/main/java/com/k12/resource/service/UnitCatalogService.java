package com.k12.resource.service;

import com.k12.common.dto.CatalogNodeVO;
import com.k12.common.dto.UnitTreeNodeVO;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 教材单元树：DB 优先 + 静态目录 + 资源表兜底
 */
@Slf4j
@Service
public class UnitCatalogService {

    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final CatalogService catalogService;
    private final UnitCatalogStaticLoader unitCatalogStaticLoader;
    public UnitCatalogService(PrimaryChineseResourceMapper primaryChineseResourceMapper, CatalogService catalogService, UnitCatalogStaticLoader unitCatalogStaticLoader) {
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.catalogService = catalogService;
        this.unitCatalogStaticLoader = unitCatalogStaticLoader;
    }


    /**
     * 获取单元树（DB 优先，静态 JSON 兜底）
     */
    public List<UnitTreeNodeVO> getUnitTree(
            String volumeKey,
            String gradeName,
            String edition,
            String subject) {
        List<CatalogNodeVO> dbTree = catalogService.getTreeFromDbOnly(
                null, "textbook_unit", volumeKey, gradeName, edition, subject);
        List<UnitTreeNodeVO> base = convertCatalogToUnitTree(dbTree);

        List<Map<String, String>> dbPairs = primaryChineseResourceMapper.findUnitLessonPairs(
                gradeName, edition, subject);

        if (!base.isEmpty()) {
            mergeDbLessons(base, dbPairs);
            return base;
        }

        base = unitCatalogStaticLoader.getTree(volumeKey, gradeName);

        if (base.isEmpty()) {
            base = buildTreeFromDbOnly(dbPairs);
        } else {
            mergeDbLessons(base, dbPairs);
        }

        if (base.isEmpty() && StringUtils.hasText(gradeName)) {
            List<String> unitNames = primaryChineseResourceMapper.findDistinctUnitNames(gradeName, edition);
            base = unitNames.stream()
                    .filter(StringUtils::hasText)
                    .map(u -> new UnitTreeNodeVO(u, List.of()))
                    .collect(Collectors.toList());
        }
        return base;
    }

    /** 仅读取 classpath unit-catalog.json（供管理端导入） */
    public List<UnitTreeNodeVO> getUnitTreeFromStaticJson(
            String volumeKey,
            String gradeName,
            String edition,
            String subject) {
        return unitCatalogStaticLoader.getTree(volumeKey, gradeName);
    }

    private List<UnitTreeNodeVO> convertCatalogToUnitTree(List<CatalogNodeVO> roots) {
        if (roots == null || roots.isEmpty()) {
            return List.of();
        }
        List<UnitTreeNodeVO> result = new ArrayList<>();
        collectUnitNodes(roots, result);
        return result;
    }

    private void collectUnitNodes(List<CatalogNodeVO> nodes, List<UnitTreeNodeVO> out) {
        for (CatalogNodeVO node : nodes) {
            if ("unit".equals(node.getNodeType())) {
                UnitTreeNodeVO unit = new UnitTreeNodeVO();
                unit.setName(node.getName());
                List<String> lessons = node.getSubUnits() != null && !node.getSubUnits().isEmpty()
                        ? new ArrayList<>(node.getSubUnits())
                        : extractLessonNames(node.getChildren());
                unit.setSubUnits(lessons);
                out.add(unit);
            }
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                collectUnitNodes(node.getChildren(), out);
            }
        }
    }

    private List<String> extractLessonNames(List<CatalogNodeVO> children) {
        if (children == null || children.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        for (CatalogNodeVO child : children) {
            if ("lesson".equals(child.getNodeType())) {
                names.add(child.getName());
            }
        }
        return names;
    }

    private void mergeDbLessons(List<UnitTreeNodeVO> tree, List<Map<String, String>> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return;
        }
        Map<String, UnitTreeNodeVO> byName = new LinkedHashMap<>();
        for (UnitTreeNodeVO node : tree) {
            byName.put(node.getName(), node);
        }
        for (Map<String, String> pair : pairs) {
            String unit = pair.get("unitName");
            String lesson = pair.get("lessonName");
            if (!StringUtils.hasText(unit) || !StringUtils.hasText(lesson)) {
                continue;
            }
            UnitTreeNodeVO node = byName.get(unit);
            if (node == null) {
                node = new UnitTreeNodeVO(unit, new ArrayList<>());
                tree.add(node);
                byName.put(unit, node);
            }
            if (!node.getSubUnits().contains(lesson)) {
                node.getSubUnits().add(lesson);
            }
        }
    }

    private List<UnitTreeNodeVO> buildTreeFromDbOnly(List<Map<String, String>> pairs) {
        Map<String, UnitTreeNodeVO> byName = new LinkedHashMap<>();
        if (pairs == null) {
            return List.of();
        }
        for (Map<String, String> pair : pairs) {
            String unit = pair.get("unitName");
            String lesson = pair.get("lessonName");
            if (!StringUtils.hasText(unit)) {
                continue;
            }
            UnitTreeNodeVO node = byName.computeIfAbsent(unit, u -> new UnitTreeNodeVO(u, new ArrayList<>()));
            if (StringUtils.hasText(lesson) && !node.getSubUnits().contains(lesson)) {
                node.getSubUnits().add(lesson);
            }
        }
        return new ArrayList<>(byName.values());
    }
}
