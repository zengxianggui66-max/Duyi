package com.k12.resource.service;

import com.k12.common.dto.SinologyUnitBundleVO;
import com.k12.common.entity.EduResource;
import com.k12.common.entity.EduSchool;
import com.k12.common.entity.SinologyReading;
import com.k12.resource.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 国学阅读·作文深度融合 Service
 * 核心逻辑：通过 edu_resource_dimension.unit_id + module_id 聚合国学素材与作文素材
 */
@Slf4j
@Service
public class SinologyService {

    private final SinologyReadingMapper sinologyReadingMapper;
    private final EduSchoolMapper eduSchoolMapper;
    private final EduResourceMapper eduResourceMapper;
    public SinologyService(SinologyReadingMapper sinologyReadingMapper, EduSchoolMapper eduSchoolMapper, EduResourceMapper eduResourceMapper) {
        this.sinologyReadingMapper = sinologyReadingMapper;
        this.eduSchoolMapper = eduSchoolMapper;
        this.eduResourceMapper = eduResourceMapper;
    }


    private static final Integer MODULE_SINOLOGY = 34;   // 国学阅读
    private static final Integer MODULE_COMPOSITION = 14; // 作文

    // ===================== 核心聚合：单元打包查询 =====================

    /**
     * 根据 unit_id 获取「国学阅读 + 作文训练」完整单元包
     * 这是核心API：GET /api/sinology/unit-bundle?unitId=xxx
     */
    public SinologyUnitBundleVO getUnitBundle(Long unitId) {
        SinologyUnitBundleVO bundle = new SinologyUnitBundleVO();

        // 1. 查单元基本信息
        SinologyUnitBundleVO.UnitInfo unitInfo = buildUnitInfo(unitId);
        bundle.setUnit(unitInfo);

        // 2. 查该单元的国学阅读素材（module_id=34）
        List<SinologyReading> sinologyList = sinologyReadingMapper.findByUnitIdAndModule(unitId, MODULE_SINOLOGY);
        bundle.setSinologyReadings(convertToCards(sinologyList));

        // 3. 查该单元的作文训练素材（module_id=14），同上查询方式
        List<SinologyReading> compositionList = sinologyReadingMapper.findByUnitIdAndModule(unitId, MODULE_COMPOSITION);
        bundle.setCompositionTrainings(convertToCards(compositionList));

        // 4. 查询该单元关联的地区+学校
        bundle.setRelatedSchools(findRelatedSchools(unitId));

        return bundle;
    }

    /**
     * 按多维度检索国学阅读素材
     */
    public List<SinologyReading> searchSinology(String gradeName, String editionName,
                                                String volumeName, String genre,
                                                String dynasty, String keyword) {
        return sinologyReadingMapper.findByFilters(gradeName, editionName, volumeName, genre, dynasty, keyword);
    }

    /**
     * 查询国学阅读详情
     */
    public SinologyReading getSinologyDetail(Long id) {
        return sinologyReadingMapper.findDetailById(id);
    }

    /**
     * 获取筛选枚举（体裁、朝代）
     */
    public Map<String, Object> getFilterEnums() {
        Map<String, Object> result = new HashMap<>();
        result.put("genres", sinologyReadingMapper.findDistinctGenres());
        result.put("dynasties", sinologyReadingMapper.findDistinctDynasties());
        return result;
    }

    // ===================== 学校相关 =====================

    /**
     * 查询学校列表（按地区/标签）
     */
    public List<Map<String, Object>> listSchools(Integer regionId, String regionPath, String tag) {
        List<EduSchool> schools;
        if (regionId != null) {
            schools = eduSchoolMapper.findByRegionId(regionId);
        } else if (regionPath != null && !regionPath.isBlank()) {
            schools = eduSchoolMapper.findByRegionPath(regionPath);
        } else if (tag != null && !tag.isBlank()) {
            schools = eduSchoolMapper.findByTag(tag);
        } else {
            return eduSchoolMapper.findAllSchools();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (EduSchool s : schools) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("shortName", s.getShortName());
            map.put("regionId", s.getRegionId());
            map.put("regionPath", s.getRegionPath());
            map.put("regionName", s.getRegionName());
            map.put("schoolType", s.getSchoolType());
            map.put("schoolLevel", s.getSchoolLevel());
            map.put("tags", s.getTags());
            result.add(map);
        }
        return result;
    }

    // ===================== 辅助方法 =====================

    /**
     * 根据 unitId 构建单元基本信息
     */
    private SinologyUnitBundleVO.UnitInfo buildUnitInfo(Long unitId) {
        // 通过 edu_resource_dimension 反查单元对应的维度信息
        List<EduResource> resources = eduResourceMapper.findByDimension(
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                unitId, null, null, 1, null);

        SinologyUnitBundleVO.UnitInfo info = SinologyUnitBundleVO.UnitInfo.builder()
                .unitId(unitId)
                .unitName("单元")
                .build();

        if (resources != null && !resources.isEmpty()) {
            EduResource first = resources.get(0);
            // 取更准确的单元名：按 unit_id 查询 edu_unit
            info.setUnitName(first.getUnitName() != null ? first.getUnitName() : "单元");
            info.setGradeName(first.getGradeName());
            info.setEditionName(first.getEditionName());
            info.setVolumeName(first.getVolumeName());
            info.setSemesterName(first.getSemesterName());
            info.setSubjectName(first.getSubjectName());
        }
        return info;
    }

    /**
     * 查询该单元关联的地区 + 学校
     * 通过 edu_resource_region 获取 region_id，再聚合学校
     */
    private List<Map<String, Object>> findRelatedSchools(Long unitId) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 查该单元的资源关联了哪些地区
        List<EduResource> regionResources = eduResourceMapper.findByDimension(
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                null, null, null, null,
                unitId, null, null, 1, null);

        if (regionResources == null || regionResources.isEmpty()) {
            return result;
        }

        // 简化处理：返回四川地区所有学校
        List<EduSchool> schools = eduSchoolMapper.findByRegionPath("四川省");
        for (EduSchool s : schools) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", s.getId());
            map.put("name", s.getName());
            map.put("shortName", s.getShortName());
            map.put("regionName", s.getRegionName());
            map.put("schoolType", s.getSchoolType());
            map.put("tags", s.getTags());
            result.add(map);
        }
        return result;
    }

    /**
     * SinologyReading -> SinologyCard 转换
     */
    private List<SinologyUnitBundleVO.SinologyCard> convertToCards(List<SinologyReading> readings) {
        if (readings == null || readings.isEmpty()) return Collections.emptyList();
        return readings.stream().map(r -> SinologyUnitBundleVO.SinologyCard.builder()
                .readingId(r.getId())
                .title(r.getTitle())
                .dynasty(r.getDynasty())
                .author(r.getAuthor())
                .genre(r.getGenre())
                .content(r.getContent())
                .translation(r.getTranslation())
                .appreciation(r.getAppreciation())
                .compositionHint(r.getCompositionHint())
                .difficulty(r.getDifficulty())
                .keyPhrases(r.getKeyPhrases())
                .wordCount(r.getWordCount())
                .audioUrl(r.getAudioUrl())
                .videoUrl(r.getVideoUrl())
                .build()).collect(Collectors.toList());
    }
}
