package com.k12.resource.service;

import com.k12.common.dto.BrowseStatsResultVO;
import com.k12.common.dto.BrowseTypeStatVO;
import com.k12.common.dto.BrowseModuleStatVO;
import com.k12.common.dto.CatalogBrowseContext;
import com.k12.common.dto.PrimaryChineseResourceQueryDTO;
import com.k12.common.dto.PrimaryChineseResourceVO;
import com.k12.common.dto.ResourceSuiteVO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.util.ResourceDisplayType;
import com.k12.resource.mapper.ResourceBrowseMapper;
import com.k12.resource.mapper.VAdminResourceMainMapper;
import com.k12.resource.util.PublicResourceQuerySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 统一资源浏览（M3：placement + catalog_node_id + 单元课文 fallback 合并）
 */
@Slf4j
@Service
public class ResourceBrowseService {

    private final CatalogService catalogService;
    private final ResourcePlacementService resourcePlacementService;
    private final ResourceBrowseMapper resourceBrowseMapper;
    private final VAdminResourceMainMapper vAdminResourceMainMapper;
    public ResourceBrowseService(CatalogService catalogService, ResourcePlacementService resourcePlacementService, ResourceBrowseMapper resourceBrowseMapper, VAdminResourceMainMapper vAdminResourceMainMapper) {
        this.catalogService = catalogService;
        this.resourcePlacementService = resourcePlacementService;
        this.resourceBrowseMapper = resourceBrowseMapper;
        this.vAdminResourceMainMapper = vAdminResourceMainMapper;
    }


    private static final Map<String, String> SUITE_TYPE_ICONS = Map.of(
            "课件", "📚", "教案", "📖", "练习", "✏️", "学案", "📘",
            "试卷", "📝", "视频", "🎬", "音频/朗读", "🎧", "知识点", "📋"
    );

    public boolean shouldUseBrowse(PrimaryChineseResourceQueryDTO dto) {
        return dto != null && dto.getCatalogNodeId() != null && dto.getCatalogNodeId() > 0;
    }

    public Map<String, Object> listByPage(PrimaryChineseResourceQueryDTO dto) {
        PublicResourceQuerySupport.applyPublicStatusDefault(dto);
        resolveEdition(dto);
        resolveGradeName(dto);
        applyDisplayTypeFilter(dto);

        CatalogBrowseContext ctx = catalogService.resolveBrowseContext(
                dto.getCatalogNodeId(), includeSubtree(dto));
        applyCatalogDefaultsToDto(dto, ctx);

        if (ctx.getNodeIds().isEmpty()) {
            return emptyPage(dto.getCurrent(), dto.getSize());
        }

        resourcePlacementService.ensurePlacementsForNodeIds(ctx.getNodeIds());

        String sortField = getSafeSortField(dto.getSortField());
        String sortOrder = "asc".equalsIgnoreCase(dto.getSortOrder()) ? "asc" : "desc";

        Page<PrimaryChineseResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<PrimaryChineseResource> result = resourceBrowseMapper.findPageByCatalogUnified(
                page,
                ctx.getNodeIds(),
                ctx.getCatalogPathPrefix(),
                ctx.getUnitNames(),
                ctx.getLessonNames(),
                ctx.getFallbackParentNodeId(),
                ctx.getTitleKeywords(),
                dto.getStage(), dto.getSubject(), dto.getModule(), dto.getType(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(), dto.getSubType(),
                dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus(),
                dto.getUploaderId(), dto.getKeyword(),
                sortField, sortOrder);

        log.debug("browse nodeIds={} total={}", ctx.getNodeIds().size(), result.getTotal());

        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        map.put("pages", result.getPages());
        return map;
    }

    public List<PrimaryChineseResource> listAll(PrimaryChineseResourceQueryDTO dto) {
        int prevCurrent = dto.getCurrent() != null ? dto.getCurrent() : 1;
        int prevSize = dto.getSize() != null ? dto.getSize() : 15;
        dto.setCurrent(1);
        dto.setSize(5000);
        Map<String, Object> page = listByPage(dto);
        dto.setCurrent(prevCurrent);
        dto.setSize(prevSize);
        @SuppressWarnings("unchecked")
        List<PrimaryChineseResource> records = (List<PrimaryChineseResource>) page.get("records");
        return records != null ? records : List.of();
    }

    public BrowseStatsResultVO stats(PrimaryChineseResourceQueryDTO dto) {
        PublicResourceQuerySupport.applyPublicStatusDefault(dto);
        resolveEdition(dto);
        resolveGradeName(dto);
        applyDisplayTypeFilter(dto);
        if (shouldUseBrowse(dto)) {
            CatalogBrowseContext ctx = catalogService.resolveBrowseContext(
                    dto.getCatalogNodeId(), includeSubtree(dto));
            applyCatalogDefaultsToDto(dto, ctx);
            if (!ctx.getNodeIds().isEmpty()) {
                return statsByCatalog(ctx, dto);
            }
            log.warn("browse stats: catalogNodeId={} resolved empty nodeIds, skip dimension fallback",
                    dto.getCatalogNodeId());
            return emptyStats();
        }
        if (hasDimensionFilters(dto)) {
            return statsByDimension(dto);
        }
        return emptyStats();
    }

    private BrowseStatsResultVO statsByCatalog(
            CatalogBrowseContext ctx, PrimaryChineseResourceQueryDTO dto) {
        resourcePlacementService.ensurePlacementsForNodeIds(ctx.getNodeIds());
        List<BrowseTypeStatVO> types = resourceBrowseMapper.statsByCatalogUnified(
                ctx.getNodeIds(),
                ctx.getCatalogPathPrefix(),
                ctx.getUnitNames(),
                ctx.getLessonNames(),
                ctx.getFallbackParentNodeId(),
                ctx.getTitleKeywords(),
                dto.getStage(), dto.getSubject(), dto.getModule(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
        Long total = resourceBrowseMapper.countByCatalogUnified(
                ctx.getNodeIds(),
                ctx.getCatalogPathPrefix(),
                ctx.getUnitNames(),
                ctx.getLessonNames(),
                ctx.getFallbackParentNodeId(),
                ctx.getTitleKeywords(),
                dto.getStage(), dto.getSubject(), dto.getModule(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
        BrowseStatsResultVO result = new BrowseStatsResultVO();
        result.setTypes(normalizeTypeStats(types));
        result.setTotal(total != null ? total : 0L);
        result.setTypeSum(sumTypeCounts(result.getTypes()));
        warnStatsMismatch(result, "catalog");
        return result;
    }

    private BrowseStatsResultVO statsByDimension(PrimaryChineseResourceQueryDTO dto) {
        List<BrowseTypeStatVO> types = resourceBrowseMapper.statsByDimension(
                dto.getStage(), dto.getSubject(), dto.getModule(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                dto.getUnitName(), dto.getLessonName(),
                dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
        Long total = resourceBrowseMapper.countByDimension(
                dto.getStage(), dto.getSubject(), dto.getModule(),
                dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                dto.getUnitName(), dto.getLessonName(),
                dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
        BrowseStatsResultVO result = new BrowseStatsResultVO();
        result.setTypes(normalizeTypeStats(types));
        result.setTotal(total != null ? total : 0L);
        result.setTypeSum(sumTypeCounts(result.getTypes()));
        warnStatsMismatch(result, "dimension");
        return result;
    }

    private BrowseStatsResultVO emptyStats() {
        BrowseStatsResultVO result = new BrowseStatsResultVO();
        result.setTypes(List.of());
        result.setTotal(0L);
        result.setTypeSum(0L);
        return result;
    }

    public List<BrowseModuleStatVO> moduleStats(PrimaryChineseResourceQueryDTO dto) {
        PublicResourceQuerySupport.applyPublicStatusDefault(dto);
        resolveEdition(dto);
        resolveGradeName(dto);
        if (shouldUseBrowse(dto)) {
            CatalogBrowseContext ctx = catalogService.resolveBrowseContext(
                    dto.getCatalogNodeId(), includeSubtree(dto));
            applyCatalogDefaultsToDto(dto, ctx);
            if (!ctx.getNodeIds().isEmpty()) {
                resourcePlacementService.ensurePlacementsForNodeIds(ctx.getNodeIds());
                return resourceBrowseMapper.moduleStatsByCatalogUnified(
                        ctx.getNodeIds(),
                        ctx.getCatalogPathPrefix(),
                        ctx.getUnitNames(),
                        ctx.getLessonNames(),
                        ctx.getFallbackParentNodeId(),
                        ctx.getTitleKeywords(),
                        dto.getStage(), dto.getSubject(), dto.getModule(),
                        dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                        dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
            }
        }
        if (hasDimensionFilters(dto)) {
            return resourceBrowseMapper.moduleStatsByDimension(
                    dto.getStage(), dto.getSubject(), dto.getModule(),
                    dto.getGradeName(), dto.getEdition(), dto.getBrandCode(),
                    dto.getUnitName(), dto.getLessonName(),
                    dto.getStatus(), dto.getAuditStatus(), dto.getPublishStatus());
        }
        return List.of();
    }

    private boolean hasDimensionFilters(PrimaryChineseResourceQueryDTO dto) {
        return StringUtils.hasText(dto.getSubject())
                || StringUtils.hasText(dto.getGradeName())
                || StringUtils.hasText(dto.getModule())
                || StringUtils.hasText(dto.getStage());
    }

    public List<ResourceSuiteVO> listSuites(PrimaryChineseResourceQueryDTO dto) {
        List<PrimaryChineseResource> records = listAll(dto);
        if (records.isEmpty()) {
            return List.of();
        }
        Map<String, List<PrimaryChineseResource>> typeMap = new LinkedHashMap<>();
        for (PrimaryChineseResource item : records) {
            String type = StringUtils.hasText(item.getType()) ? item.getType() : "其他";
            typeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(item);
        }
        String gradeLabel = dto.getGradeName() != null ? dto.getGradeName() : "";
        String editionLabel = dto.getEdition() != null ? dto.getEdition() : "";
        List<ResourceSuiteVO> suites = new ArrayList<>();
        for (Map.Entry<String, List<PrimaryChineseResource>> entry : typeMap.entrySet()) {
            String type = entry.getKey();
            List<PrimaryChineseResource> items = entry.getValue();
            ResourceSuiteVO suite = new ResourceSuiteVO();
            suite.setKey(type);
            suite.setIcon(SUITE_TYPE_ICONS.getOrDefault(type, "📄"));
            suite.setTitle("成套" + type + "资源");
            suite.setSub(gradeLabel + editionLabel + type + "合集（共" + items.size() + "份）");
            suite.setTag("配套资源");
            suite.setFileCount(items.size());
            String updateTime = items.stream()
                    .map(PrimaryChineseResource::getUploadTime)
                    .filter(Objects::nonNull)
                    .map(t -> t.toLocalDate().toString())
                    .findFirst()
                    .orElse(LocalDateTime.now().toLocalDate().toString());
            suite.setUpdateTime(updateTime);
            suite.setItems(items.stream()
                    .map(PrimaryChineseResourceVO::fromEntity)
                    .collect(Collectors.toList()));
            suites.add(suite);
        }
        return suites;
    }

    private void resolveEdition(PrimaryChineseResourceQueryDTO dto) {
        String edition = normalizeLabel(dto.getEdition());
        if (!StringUtils.hasText(edition)) {
            return;
        }
        dto.setEdition(edition);
        List<String> allEditions = vAdminResourceMainMapper.findDistinctEditionsBySourceType("primary_chinese");
        if (allEditions.contains(edition)) {
            return;
        }
        for (String e : allEditions) {
            if (e == null) {
                continue;
            }
            String ne = normalizeLabel(e);
            if (ne.equals(edition) || e.contains(edition) || edition.contains(e)) {
                dto.setEdition(e);
                return;
            }
        }
        log.debug("browse: no matching edition in DB for '{}', skip edition filter", edition);
        dto.setEdition(null);
    }

    private void resolveGradeName(PrimaryChineseResourceQueryDTO dto) {
        String grade = normalizeLabel(dto.getGradeName());
        if (!StringUtils.hasText(grade)) {
            return;
        }
        dto.setGradeName(grade);
        List<String> allGrades = vAdminResourceMainMapper.findDistinctGradesBySourceType("primary_chinese");
        if (allGrades.contains(grade)) {
            return;
        }
        for (String g : allGrades) {
            if (g == null) {
                continue;
            }
            String ng = normalizeLabel(g);
            if (ng.equals(grade) || g.contains(grade) || grade.contains(g)) {
                dto.setGradeName(g);
                return;
            }
        }
        log.debug("browse: no matching grade in DB for '{}', skip grade filter", grade);
        dto.setGradeName(null);
    }

    private static String normalizeLabel(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim()
                .replace('（', '(')
                .replace('）', ')')
                .replaceAll("\\s+", "");
    }

    private String getSafeSortField(String sortField) {
        if (sortField == null) {
            return "upload_time";
        }
        return switch (sortField) {
            case "upload_time", "file_size_kb", "download_count", "view_count", "sort" -> sortField;
            default -> "upload_time";
        };
    }

    /**
     * 目录浏览时以节点 meta.defaultModule 为准（如期末复习/国学阅读包），避免顶栏仍停留在「同步备课」时 module 错配。
     * 不自动注入 type/subType（避免「全部」Tab 被节点 meta 误过滤）。
     */
    private void applyCatalogDefaultsToDto(PrimaryChineseResourceQueryDTO dto, CatalogBrowseContext ctx) {
        if (StringUtils.hasText(ctx.getDefaultModule())) {
            dto.setModule(ctx.getDefaultModule());
        }
        // unitName/lessonName 勿写入 dto：CATALOG_MATCH_OR 已用 ctx 列表匹配；
        // 此处再设精确等于会过滤掉 OSS 别名单元（·/：）及旧课文名（「天地人」vs「1 天地人」）。
    }

    private boolean includeSubtree(PrimaryChineseResourceQueryDTO dto) {
        if (shouldUseBrowse(dto)) {
            return catalogService.resolveIncludeSubtreeForNode(
                    dto.getCatalogNodeId(), dto.getIncludeSubtree());
        }
        return dto.getIncludeSubtree() == null || Boolean.TRUE.equals(dto.getIncludeSubtree());
    }

    /**
     * displayType Tab → type/subType；与 stats 分组、前端 Tab 一致
     */
    private void applyDisplayTypeFilter(PrimaryChineseResourceQueryDTO dto) {
        if (dto == null) {
            return;
        }
        String displayType = dto.getDisplayType();
        if (!StringUtils.hasText(displayType)) {
            if (StringUtils.hasText(dto.getType())) {
                ResourceDisplayType.QueryFilter inferred =
                        ResourceDisplayType.resolveQueryFilter(dto.getType());
                if (!inferred.isAll()
                        && !dto.getType().equals(inferred.type())
                        && !StringUtils.hasText(dto.getSubType())) {
                    dto.setType(inferred.type());
                    dto.setSubType(inferred.subType());
                }
            }
            return;
        }
        ResourceDisplayType.QueryFilter filter =
                ResourceDisplayType.resolveQueryFilter(displayType);
        if (filter.isAll()) {
            dto.setType(null);
            dto.setSubType(null);
            return;
        }
        dto.setType(filter.type());
        dto.setSubType(filter.subType());
    }

    private List<BrowseTypeStatVO> normalizeTypeStats(List<BrowseTypeStatVO> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<BrowseTypeStatVO> out = new ArrayList<>(raw.size());
        for (BrowseTypeStatVO vo : raw) {
            if (vo == null) {
                continue;
            }
            String displayType = StringUtils.hasText(vo.getDisplayType())
                    ? vo.getDisplayType()
                    : ResourceDisplayType.resolveDisplayType(vo.getType(), null);
            if (!StringUtils.hasText(displayType)) {
                displayType = "其他";
            }
            vo.setDisplayType(displayType);
            vo.setType(displayType);
            out.add(vo);
        }
        return out;
    }

    private long sumTypeCounts(List<BrowseTypeStatVO> types) {
        if (types == null) {
            return 0L;
        }
        long sum = 0L;
        for (BrowseTypeStatVO vo : types) {
            if (vo != null && vo.getCount() != null) {
                sum += vo.getCount();
            }
        }
        return sum;
    }

    private void warnStatsMismatch(BrowseStatsResultVO result, String scope) {
        if (result == null || result.getTotal() == null || result.getTypeSum() == null) {
            return;
        }
        if (!result.getTotal().equals(result.getTypeSum())) {
            log.warn("browse stats mismatch [{}]: total={} typeSum={}",
                    scope, result.getTotal(), result.getTypeSum());
        }
    }

    private Map<String, Object> emptyPage(int current, int size) {
        Map<String, Object> map = new HashMap<>();
        map.put("records", List.of());
        map.put("total", 0L);
        map.put("current", current);
        map.put("size", size);
        map.put("pages", 0L);
        return map;
    }
}
