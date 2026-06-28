package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.*;
import com.k12.common.entity.HomeLatestColumn;
import com.k12.common.entity.HomeLatestItem;
import com.k12.common.service.AdminPermissionService;
import com.k12.resource.mapper.HomeLatestColumnMapper;
import com.k12.resource.mapper.HomeLatestItemMapper;
import com.k12.resource.mapper.HomePanelResourceMapper;
import com.k12.resource.service.HomeLatestColumnService;
import com.k12.resource.util.StageKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@SuppressWarnings("null")
public class HomeLatestColumnServiceImpl implements HomeLatestColumnService {

    private static final String PERM_EDIT = "admin:home:edit";
    private static final int DEFAULT_LIMIT = 8;
    private static final int MAX_LIMIT = 20;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Set<String> DATA_SOURCES = Set.of("rule", "manual", "api");
    private static final Set<String> RESOURCE_SOURCES = Set.of(
            "edu_resource", "oss_primary_chinese", "edu_resource_suite");

    private final HomeLatestColumnMapper columnMapper;
    private final HomeLatestItemMapper itemMapper;
    private final HomePanelResourceMapper resourceMapper;
    private final AdminPermissionService adminPermissionService;
    private final ObjectMapper objectMapper;
    public HomeLatestColumnServiceImpl(HomeLatestColumnMapper columnMapper, HomeLatestItemMapper itemMapper, HomePanelResourceMapper resourceMapper, AdminPermissionService adminPermissionService, ObjectMapper objectMapper) {
        this.columnMapper = columnMapper;
        this.itemMapper = itemMapper;
        this.resourceMapper = resourceMapper;
        this.adminPermissionService = adminPermissionService;
        this.objectMapper = objectMapper;
    }


    @Override
    public List<HomeLatestColumnWithItemsVO> listPublicColumns(String stageKey) {
        List<HomeLatestColumn> rows = listColumns(false);
        List<HomeLatestColumnWithItemsVO> result = new ArrayList<>();
        for (HomeLatestColumn row : rows) {
            HomeLatestColumnWithItemsVO vo = new HomeLatestColumnWithItemsVO();
            vo.setKey(row.getColumnKey());
            vo.setTitle(row.getTitle());
            vo.setMorePath(row.getMorePath());
            vo.setDataSource(row.getDataSource());
            if ("api".equals(row.getDataSource())) {
                vo.setItems(List.of());
            } else {
                vo.setItems(resolveItems(row, stageKey));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<HomeLatestColumnVO> listAdminColumns(boolean includeDisabled) {
        List<HomeLatestColumnVO> result = new ArrayList<>();
        for (HomeLatestColumn row : listColumns(includeDisabled)) {
            result.add(mapColumn(row));
        }
        return result;
    }

    @Override
    @Transactional
    public HomeLatestColumnVO updateColumn(Long id, HomeLatestColumnWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeLatestColumn row = requireColumn(id);
        validateWrite(dto);
        applyColumn(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        columnMapper.updateById(row);
        return mapColumn(row);
    }

    @Override
    @Transactional
    public void updateColumnStatus(Long id, int status, Long adminUserId) {
        requireEdit(adminUserId);
        HomeLatestColumn row = requireColumn(id);
        row.setStatus(status);
        row.setUpdateTime(LocalDateTime.now());
        columnMapper.updateById(row);
    }

    @Override
    public List<HomeLatestItemVO> listManualItems(Long columnId, boolean includeDisabled) {
        requireColumn(columnId);
        return loadManualItems(columnId, includeDisabled);
    }

    @Override
    @Transactional
    public HomeLatestItemVO createManualItem(Long columnId, HomeLatestItemWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeLatestColumn column = requireColumn(columnId);
        if (!"manual".equals(column.getDataSource())) {
            throw new BusinessException(400, "仅 manual 数据源列可维护手工条目");
        }
        validateManualItemWrite(dto);
        HomeLatestItem row = new HomeLatestItem();
        row.setColumnId(columnId);
        applyManualItem(row, dto);
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        itemMapper.insert(row);
        return mapManualItem(row);
    }

    @Override
    @Transactional
    public HomeLatestItemVO updateManualItem(Long itemId, HomeLatestItemWriteDTO dto, Long adminUserId) {
        requireEdit(adminUserId);
        HomeLatestItem row = requireManualItem(itemId);
        validateManualItemWrite(dto);
        applyManualItem(row, dto);
        row.setUpdateTime(LocalDateTime.now());
        itemMapper.updateById(row);
        return mapManualItem(row);
    }

    @Override
    @Transactional
    public void deleteManualItem(Long itemId, Long adminUserId) {
        requireEdit(adminUserId);
        requireManualItem(itemId);
        itemMapper.deleteById(itemId);
    }

    @Override
    public List<HomeLatestItemVO> previewColumn(Long columnId, String stageKey) {
        HomeLatestColumn row = requireColumn(columnId);
        if ("api".equals(row.getDataSource())) {
            return List.of();
        }
        return resolveItems(row, stageKey);
    }

    private List<HomeLatestColumn> listColumns(boolean includeDisabled) {
        LambdaQueryWrapper<HomeLatestColumn> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(HomeLatestColumn::getStatus, 1);
        }
        wrapper.orderByDesc(HomeLatestColumn::getSort).orderByAsc(HomeLatestColumn::getId);
        return columnMapper.selectList(wrapper);
    }

    private List<HomeLatestItemVO> resolveItems(HomeLatestColumn column, String stageKey) {
        return switch (column.getDataSource()) {
            case "manual" -> loadManualItems(column.getId(), false);
            case "rule" -> queryByRule(parseRule(column.getRuleJson()), stageKey);
            default -> List.of();
        };
    }

    private List<HomeLatestItemVO> queryByRule(HomeLatestRuleDTO rule, String stageKey) {
        if (rule == null) {
            return List.of();
        }
        int limit = resolveLimit(rule.getLimit());
        String effectiveStageKey = StringUtils.hasText(rule.getStageKey()) ? rule.getStageKey().trim() : stageKey;
        String stageName = StageKeyHelper.toStageName(effectiveStageKey);
        List<String> moduleNames = safeList(rule.getModuleNames());
        List<String> excludeModuleNames = safeList(rule.getExcludeModuleNames());
        List<String> resourceTypeNames = safeList(rule.getResourceTypeNames());
        String titleKeyword = trimOrNull(rule.getTitleKeyword());
        String subjectName = trimOrNull(rule.getSubjectName());

        LinkedHashMap<Long, HomeLatestItemVO> merged = new LinkedHashMap<>();
        int fetchEach = limit + 10;

        if (StringUtils.hasText(stageName)) {
            List<Map<String, Object>> eduRows = resourceMapper.findEduResources(
                    stageName, subjectName, null, null, null,
                    moduleNames, excludeModuleNames, resourceTypeNames, titleKeyword,
                    null, fetchEach);
            appendResourceRows(merged, eduRows, "edu_resource", limit);
        }

        if (merged.size() < limit && StringUtils.hasText(stageName)) {
            List<Map<String, Object>> ossRows = resourceMapper.findOssResources(
                    stageName, subjectName, null, null,
                    moduleNames, excludeModuleNames, resourceTypeNames, titleKeyword,
                    fetchEach);
            appendResourceRows(merged, ossRows, "oss_primary_chinese", limit);
        }

        return new ArrayList<>(merged.values());
    }

    private void appendResourceRows(
            LinkedHashMap<Long, HomeLatestItemVO> merged,
            List<Map<String, Object>> rows,
            String source,
            int limit) {
        if (rows == null) {
            return;
        }
        for (Map<String, Object> row : rows) {
            if (merged.size() >= limit) {
                return;
            }
            HomeLatestItemVO item = mapResourceRow(row, source);
            if (item != null && item.getResourceId() != null && !merged.containsKey(item.getResourceId())) {
                merged.put(item.getResourceId(), item);
            }
        }
    }

    private HomeLatestItemVO mapResourceRow(Map<String, Object> row, String source) {
        if (row == null || row.get("id") == null || row.get("title") == null) {
            return null;
        }
        long id = ((Number) row.get("id")).longValue();
        HomeLatestItemVO vo = new HomeLatestItemVO();
        vo.setId(id);
        vo.setResourceId(id);
        vo.setResourceSource(source);
        vo.setTitle(String.valueOf(row.get("title")));
        vo.setDate(formatUploadTime(row.get("upload_time")));
        vo.setItemType("resource");
        vo.setLinkPath("/resource/" + id);
        return vo;
    }

    private List<HomeLatestItemVO> loadManualItems(Long columnId, boolean includeDisabled) {
        LambdaQueryWrapper<HomeLatestItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeLatestItem::getColumnId, columnId);
        if (!includeDisabled) {
            wrapper.eq(HomeLatestItem::getStatus, 1);
        }
        wrapper.orderByDesc(HomeLatestItem::getSort).orderByDesc(HomeLatestItem::getId);
        List<HomeLatestItemVO> result = new ArrayList<>();
        for (HomeLatestItem row : itemMapper.selectList(wrapper)) {
            result.add(mapManualItem(row));
        }
        return result;
    }

    private HomeLatestItemVO mapManualItem(HomeLatestItem row) {
        HomeLatestItemVO vo = new HomeLatestItemVO();
        vo.setId(row.getId());
        vo.setTitle(row.getTitle());
        vo.setDate(row.getItemDate() != null ? row.getItemDate().format(DATE_FMT) : null);
        vo.setResourceId(row.getResourceId());
        vo.setResourceSource(row.getResourceSource());
        vo.setArticleId(row.getArticleId());
        vo.setLinkPath(row.getLinkPath());
        if (row.getResourceId() != null) {
            vo.setItemType("resource");
            if (!StringUtils.hasText(vo.getLinkPath())) {
                vo.setLinkPath("/resource/" + row.getResourceId());
            }
        } else if (row.getArticleId() != null) {
            vo.setItemType("article");
            if (!StringUtils.hasText(vo.getLinkPath())) {
                vo.setLinkPath("/news/" + row.getArticleId());
            }
        } else {
            vo.setItemType("link");
        }
        return vo;
    }

    private void validateWrite(HomeLatestColumnWriteDTO dto) {
        String dataSource = dto.getDataSource() != null ? dto.getDataSource().trim() : "";
        if (!DATA_SOURCES.contains(dataSource)) {
            throw new BusinessException(400, "data_source 仅支持 rule、manual、api");
        }
        if ("rule".equals(dataSource) && dto.getRule() == null) {
            throw new BusinessException(400, "rule 数据源须填写 rule 配置");
        }
    }

    private void validateManualItemWrite(HomeLatestItemWriteDTO dto) {
        if (dto.getResourceId() != null) {
            String source = dto.getResourceSource() != null ? dto.getResourceSource().trim() : "";
            if (!RESOURCE_SOURCES.contains(source)) {
                throw new BusinessException(400, "resource_source 无效");
            }
            if (resourceMapper.findEduById(dto.getResourceId()) == null
                    && resourceMapper.findOssById(dto.getResourceId()) == null
                    && resourceMapper.findSuiteById(dto.getResourceId()) == null) {
                throw new BusinessException(400, "资源不存在或未上架");
            }
        }
    }

    private void applyColumn(HomeLatestColumn row, HomeLatestColumnWriteDTO dto) {
        row.setTitle(dto.getTitle().trim());
        row.setMorePath(dto.getMorePath().trim());
        row.setDataSource(dto.getDataSource().trim());
        row.setRuleJson("rule".equals(row.getDataSource()) ? ruleToJson(dto.getRule()) : null);
        row.setSort(dto.getSort() != null ? dto.getSort() : row.getSort());
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : row.getStatus());
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private void applyManualItem(HomeLatestItem row, HomeLatestItemWriteDTO dto) {
        row.setTitle(dto.getTitle().trim());
        row.setItemDate(parseDate(dto.getItemDate()));
        row.setResourceId(dto.getResourceId());
        row.setResourceSource(trimOrNull(dto.getResourceSource()));
        row.setLinkPath(trimOrNull(dto.getLinkPath()));
        row.setArticleId(dto.getArticleId());
        row.setSort(dto.getSort() != null ? dto.getSort() : 0);
        row.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);
        row.setRemark(trimOrNull(dto.getRemark()));
    }

    private HomeLatestColumnVO mapColumn(HomeLatestColumn row) {
        HomeLatestColumnVO vo = new HomeLatestColumnVO();
        vo.setId(row.getId());
        vo.setColumnKey(row.getColumnKey());
        vo.setTitle(row.getTitle());
        vo.setMorePath(row.getMorePath());
        vo.setDataSource(row.getDataSource());
        vo.setRule(parseRule(row.getRuleJson()));
        vo.setSort(row.getSort());
        vo.setStatus(row.getStatus());
        vo.setRemark(row.getRemark());
        return vo;
    }

    private HomeLatestRuleDTO parseRule(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }
        try {
            return objectMapper.readValue(json, HomeLatestRuleDTO.class);
        } catch (JsonProcessingException e) {
            log.warn("invalid home latest rule_json: {}", json);
            return null;
        }
    }

    private String ruleToJson(HomeLatestRuleDTO rule) {
        if (rule == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(rule);
        } catch (JsonProcessingException e) {
            throw new BusinessException(400, "rule 配置无法序列化");
        }
    }

    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private List<String> safeList(List<String> list) {
        return list != null ? list : List.of();
    }

    private String formatUploadTime(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof LocalDateTime ldt) {
            return ldt.format(DATE_FMT);
        }
        if (value instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().format(DATE_FMT);
        }
        String s = value.toString();
        return s.length() >= 10 ? s.substring(0, 10) : s;
    }

    private LocalDate parseDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value.trim(), DATE_FMT);
    }

    private HomeLatestColumn requireColumn(Long id) {
        HomeLatestColumn row = columnMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "最新内容列不存在");
        }
        return row;
    }

    private HomeLatestItem requireManualItem(Long id) {
        HomeLatestItem row = itemMapper.selectById(id);
        if (row == null) {
            throw new BusinessException(404, "手工条目不存在");
        }
        return row;
    }

    private void requireEdit(Long adminUserId) {
        if (adminUserId == null) {
            throw new BusinessException(401, "请先登录");
        }
        if (!adminPermissionService.hasPermission(adminUserId, PERM_EDIT)) {
            throw new BusinessException(403, "无首页配置编辑权限");
        }
    }

    private String trimOrNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
