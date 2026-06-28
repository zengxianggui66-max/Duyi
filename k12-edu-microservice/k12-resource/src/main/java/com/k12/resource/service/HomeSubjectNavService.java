package com.k12.resource.service;

import com.k12.common.dto.HomeSubjectNavVO;
import com.k12.common.dto.TaxonomyEditionVO;
import com.k12.common.dto.TaxonomyResourceTypeVO;
import com.k12.common.entity.EduStage;
import com.k12.common.entity.EduSubject;
import com.k12.resource.mapper.EduModuleSubjectMapper;
import com.k12.resource.mapper.EduSubjectResourceTypeMapper;
import com.k12.resource.util.ModuleNavGroupResolver;
import com.k12.resource.util.ModuleNavGroupResolver.NavGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 5-F: home subject detail overlay aggregation.
 */
@Slf4j
@Service
public class HomeSubjectNavService {

    private final TaxonomyReadService taxonomyReadService;
    private final EduModuleSubjectMapper eduModuleSubjectMapper;
    private final EduSubjectResourceTypeMapper eduSubjectResourceTypeMapper;
    public HomeSubjectNavService(TaxonomyReadService taxonomyReadService, EduModuleSubjectMapper eduModuleSubjectMapper, EduSubjectResourceTypeMapper eduSubjectResourceTypeMapper) {
        this.taxonomyReadService = taxonomyReadService;
        this.eduModuleSubjectMapper = eduModuleSubjectMapper;
        this.eduSubjectResourceTypeMapper = eduSubjectResourceTypeMapper;
    }


    public HomeSubjectNavVO getSubjectNav(String stage, String subject) {
        HomeSubjectNavVO vo = new HomeSubjectNavVO();

        EduSubject subjectRow = taxonomyReadService.resolveSubjectForNav(stage, subject);
        if (subjectRow == null) {
            log.warn("subject-nav: unknown stage={} subject={}", stage, subject);
            return vo;
        }

        EduStage stageRow = taxonomyReadService.resolveStageForNav(stage);
        if (stageRow == null && subjectRow.getStageId() != null) {
            stageRow = taxonomyReadService.findStageById(subjectRow.getStageId());
        }

        HomeSubjectNavVO.HomeSubjectNavSubjectVO subjectVo = new HomeSubjectNavVO.HomeSubjectNavSubjectVO();
        subjectVo.setId(subjectRow.getId());
        subjectVo.setCode(subjectRow.getCode());
        subjectVo.setName(subjectRow.getName());
        subjectVo.setIcon(subjectRow.getIcon());
        if (stageRow != null) {
            subjectVo.setStageCode(stageRow.getCode());
            subjectVo.setStageName(stageRow.getName());
        }
        vo.setSubject(subjectVo);

        HomeSubjectNavVO.HomeSubjectNavSyncPrepVO syncPrep = new HomeSubjectNavVO.HomeSubjectNavSyncPrepVO();
        syncPrep.setEditions(mapEditions(taxonomyReadService.listEditions(stage, subject, false)));
        syncPrep.setResourceTypes(mapResourceTypes(loadResourceTypes(subjectRow.getId(), stage, subject)));
        vo.setSyncPrep(syncPrep);

        List<Map<String, Object>> moduleRows = loadModules(subjectRow, stageRow);
        List<HomeSubjectNavVO.HomeSubjectNavModuleItemVO> review = new ArrayList<>();
        List<HomeSubjectNavVO.HomeSubjectNavModuleItemVO> promotion = new ArrayList<>();

        for (Map<String, Object> row : moduleRows) {
            NavGroup group = ModuleNavGroupResolver.resolve(
                    stringVal(row.get("moduleCategory")),
                    stringVal(row.get("code")),
                    stringVal(row.get("name")),
                    stageRow);
            if (group == NavGroup.SYNC) {
                continue;
            }
            HomeSubjectNavVO.HomeSubjectNavModuleItemVO item = mapModule(row);
            if (group == NavGroup.PROMOTION) {
                promotion.add(item);
            } else {
                review.add(item);
            }
        }

        HomeSubjectNavVO.HomeSubjectNavModuleSectionVO reviewSection = new HomeSubjectNavVO.HomeSubjectNavModuleSectionVO();
        reviewSection.setLabel("试题试卷");
        reviewSection.setTitle("复习备考");
        reviewSection.setModules(review);
        vo.setReviewPrep(reviewSection);

        HomeSubjectNavVO.HomeSubjectNavModuleSectionVO promotionSection = new HomeSubjectNavVO.HomeSubjectNavModuleSectionVO();
        String promoTitle = ModuleNavGroupResolver.promotionTitle(stageRow);
        promotionSection.setLabel(promoTitle);
        promotionSection.setTitle(promoTitle);
        promotionSection.setModules(promotion);
        vo.setPromotionPrep(promotionSection);

        return vo;
    }

    private List<Map<String, Object>> loadModules(EduSubject subjectRow, EduStage stageRow) {
        if (stageRow == null || subjectRow.getId() == null) {
            return List.of();
        }
        Long boundCount = eduModuleSubjectMapper.countBySubjectId(subjectRow.getId());
        if (boundCount != null && boundCount > 0) {
            return eduModuleSubjectMapper.selectModulesForSubject(subjectRow.getId(), stageRow.getId());
        }
        return eduModuleSubjectMapper.selectStageModules(stageRow.getId());
    }

    private List<TaxonomyResourceTypeVO> loadResourceTypes(Integer subjectId, String stage, String subject) {
        Long boundCount = eduSubjectResourceTypeMapper.countBySubjectId(subjectId);
        if (boundCount != null && boundCount > 0) {
            List<Map<String, Object>> rows = eduSubjectResourceTypeMapper.selectTypesForSubject(subjectId);
            List<TaxonomyResourceTypeVO> list = new ArrayList<>(rows.size());
            for (Map<String, Object> row : rows) {
                TaxonomyResourceTypeVO item = new TaxonomyResourceTypeVO();
                item.setId(intVal(row.get("id")));
                item.setCode(stringVal(row.get("code")));
                item.setName(stringVal(row.get("name")));
                item.setSort(intVal(row.get("sort")));
                list.add(item);
            }
            return list;
        }
        return taxonomyReadService.listResourceTypes(stage, subject, "同步备课", false);
    }

    private List<HomeSubjectNavVO.HomeSubjectNavEditionItemVO> mapEditions(List<TaxonomyEditionVO> editions) {
        List<HomeSubjectNavVO.HomeSubjectNavEditionItemVO> list = new ArrayList<>();
        Map<String, HomeSubjectNavVO.HomeSubjectNavEditionItemVO> dedup = new LinkedHashMap<>();
        for (TaxonomyEditionVO edition : editions) {
            if (edition == null || !StringUtils.hasText(edition.getName())) {
                continue;
            }
            String key = edition.getCode() != null ? edition.getCode() : edition.getName();
            HomeSubjectNavVO.HomeSubjectNavEditionItemVO item = new HomeSubjectNavVO.HomeSubjectNavEditionItemVO();
            item.setId(edition.getId());
            item.setCode(edition.getCode());
            item.setName(edition.getName());
            item.setSort(edition.getSort());
            item.setIsNew(inferEditionIsNew(edition.getName(), edition.getYearLabel()));
            dedup.putIfAbsent(key, item);
        }
        list.addAll(dedup.values());
        return list;
    }

    private List<HomeSubjectNavVO.HomeSubjectNavResourceTypeItemVO> mapResourceTypes(List<TaxonomyResourceTypeVO> types) {
        List<HomeSubjectNavVO.HomeSubjectNavResourceTypeItemVO> list = new ArrayList<>(types.size());
        for (TaxonomyResourceTypeVO type : types) {
            if (type == null || !StringUtils.hasText(type.getName())) {
                continue;
            }
            HomeSubjectNavVO.HomeSubjectNavResourceTypeItemVO item = new HomeSubjectNavVO.HomeSubjectNavResourceTypeItemVO();
            item.setId(type.getId());
            item.setCode(type.getCode());
            item.setName(type.getName());
            item.setSort(type.getSort());
            list.add(item);
        }
        return list;
    }

    private HomeSubjectNavVO.HomeSubjectNavModuleItemVO mapModule(Map<String, Object> row) {
        HomeSubjectNavVO.HomeSubjectNavModuleItemVO item = new HomeSubjectNavVO.HomeSubjectNavModuleItemVO();
        item.setId(intVal(row.get("id")));
        item.setCode(stringVal(row.get("code")));
        item.setName(stringVal(row.get("name")));
        item.setIcon(stringVal(row.get("icon")));
        item.setSort(intVal(row.get("sort")));
        return item;
    }

    private boolean inferEditionIsNew(String name, String yearLabel) {
        if (StringUtils.hasText(yearLabel)) {
            String y = yearLabel.trim();
            if (y.contains("2024") || y.contains("2025")) {
                return true;
            }
        }
        if (!StringUtils.hasText(name)) {
            return false;
        }
        String n = name.trim();
        return n.contains("(2024)") || n.contains("(2025)")
                || n.contains("（2024）") || n.contains("（2025）");
    }

    private String stringVal(Object value) {
        return value != null ? String.valueOf(value) : null;
    }

    private Integer intVal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
