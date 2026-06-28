package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.dto.DictionaryItemVO;
import com.k12.common.entity.*;
import com.k12.resource.mapper.*;
import com.k12.resource.service.DictionaryReadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phase 5-D：字典/标签统一读服务（DB 优先 + 内置兜底）
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class DictionaryReadServiceImpl implements DictionaryReadService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final EduExamSceneMapper eduExamSceneMapper;
    private final EduTeachingSceneMapper eduTeachingSceneMapper;
    private final EduFileFormatMapper eduFileFormatMapper;
    private final EduRegionMapper eduRegionMapper;
    private final EduBrowseTagMapper eduBrowseTagMapper;
    public DictionaryReadServiceImpl(EduExamSceneMapper eduExamSceneMapper, EduTeachingSceneMapper eduTeachingSceneMapper, EduFileFormatMapper eduFileFormatMapper, EduRegionMapper eduRegionMapper, EduBrowseTagMapper eduBrowseTagMapper) {
        this.eduExamSceneMapper = eduExamSceneMapper;
        this.eduTeachingSceneMapper = eduTeachingSceneMapper;
        this.eduFileFormatMapper = eduFileFormatMapper;
        this.eduRegionMapper = eduRegionMapper;
        this.eduBrowseTagMapper = eduBrowseTagMapper;
    }


    @Override
    public List<DictionaryItemVO> listExamScenes(boolean includeDisabled) {
        List<EduExamScene> rows = queryExamScenes(includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_exam_scene 无数据，返回内置考试场景兜底");
            return fallbackExamScenes();
        }
        return rows.stream().map(DictionaryItemVO::fromExamScene).toList();
    }

    @Override
    public List<DictionaryItemVO> listTeachingScenes(boolean includeDisabled) {
        List<EduTeachingScene> rows = queryTeachingScenes(includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_teaching_scene 无数据，返回内置教学场景兜底");
            return fallbackTeachingScenes();
        }
        return rows.stream().map(DictionaryItemVO::fromTeachingScene).toList();
    }

    @Override
    public List<DictionaryItemVO> listFileFormats(boolean includeDisabled) {
        List<EduFileFormat> rows = queryFileFormats(includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_file_format 无数据，返回内置文件格式兜底");
            return fallbackFileFormats();
        }
        return rows.stream().map(DictionaryItemVO::fromFileFormat).toList();
    }

    @Override
    public List<DictionaryItemVO> listRegions(Integer parentId, boolean includeDisabled) {
        List<EduRegion> rows = queryRegions(parentId, includeDisabled);
        if (rows.isEmpty() && parentId == null) {
            log.warn("edu_region 无数据，返回内置地区兜底");
            return fallbackRegions();
        }
        return rows.stream().map(DictionaryItemVO::fromRegion).toList();
    }

    @Override
    public List<DictionaryItemVO> listBrowseTags(String stage, String module, boolean includeDisabled) {
        List<EduBrowseTag> rows = queryBrowseTags(includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_browse_tag 无数据，返回内置资源标签兜底");
            return filterBrowseTags(fallbackBrowseTags(), stage, module);
        }
        List<DictionaryItemVO> all = new ArrayList<>(rows.size());
        for (EduBrowseTag row : rows) {
            all.add(DictionaryItemVO.fromBrowseTag(row, parseJsonList(row.getApplicableStages()),
                    parseJsonList(row.getApplicableModules())));
        }
        return filterBrowseTags(all, stage, module);
    }

    private List<EduExamScene> queryExamScenes(boolean includeDisabled) {
        LambdaQueryWrapper<EduExamScene> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduExamScene::getStatus, 1);
        }
        wrapper.orderByAsc(EduExamScene::getSort).orderByAsc(EduExamScene::getId);
        return eduExamSceneMapper.selectList(wrapper);
    }

    private List<EduTeachingScene> queryTeachingScenes(boolean includeDisabled) {
        LambdaQueryWrapper<EduTeachingScene> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduTeachingScene::getStatus, 1);
        }
        wrapper.orderByAsc(EduTeachingScene::getSort).orderByAsc(EduTeachingScene::getId);
        return eduTeachingSceneMapper.selectList(wrapper);
    }

    private List<EduFileFormat> queryFileFormats(boolean includeDisabled) {
        LambdaQueryWrapper<EduFileFormat> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduFileFormat::getStatus, 1);
        }
        wrapper.orderByAsc(EduFileFormat::getSort).orderByAsc(EduFileFormat::getId);
        return eduFileFormatMapper.selectList(wrapper);
    }

    private List<EduRegion> queryRegions(Integer parentId, boolean includeDisabled) {
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

    private List<EduBrowseTag> queryBrowseTags(boolean includeDisabled) {
        LambdaQueryWrapper<EduBrowseTag> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduBrowseTag::getStatus, 1);
        }
        wrapper.orderByAsc(EduBrowseTag::getSort).orderByAsc(EduBrowseTag::getId);
        return eduBrowseTagMapper.selectList(wrapper);
    }

    private List<DictionaryItemVO> filterBrowseTags(List<DictionaryItemVO> tags, String stage, String module) {
        if (!StringUtils.hasText(stage) && !StringUtils.hasText(module)) {
            return tags;
        }
        List<DictionaryItemVO> result = new ArrayList<>();
        for (DictionaryItemVO tag : tags) {
            if (StringUtils.hasText(stage) && tag.getApplicableStages() != null && !tag.getApplicableStages().isEmpty()
                    && !tag.getApplicableStages().contains(stage)) {
                continue;
            }
            if (StringUtils.hasText(module) && tag.getApplicableModules() != null && !tag.getApplicableModules().isEmpty()
                    && !tag.getApplicableModules().contains(module)) {
                continue;
            }
            result.add(tag);
        }
        return result;
    }

    private List<String> parseJsonList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private List<DictionaryItemVO> fallbackExamScenes() {
        return List.of(
                item("unit", "单元测验", 1, "unit"),
                item("monthly", "月考", 2, "monthly"),
                item("midterm", "期中考试", 3, "midterm"),
                item("final", "期末考试", 4, "final"),
                item("xsc", "小升初", 5, "xsc"),
                item("zk", "中考", 6, "zk"),
                item("gk", "高考", 7, "gk"),
                item("competition", "竞赛", 10, "competition")
        );
    }

    private List<DictionaryItemVO> fallbackTeachingScenes() {
        return List.of(
                scene("preview", "预习", 1),
                scene("new_lesson", "新课", 2),
                scene("practice", "练习", 3),
                scene("review", "复习", 4),
                scene("open_class", "公开课", 5),
                scene("exam_prep", "备考", 6)
        );
    }

    private List<DictionaryItemVO> fallbackFileFormats() {
        return List.of(
                format("word", "Word文档", "doc,docx", "office", 1),
                format("ppt", "PPT演示", "ppt,pptx", "office", 2),
                format("pdf", "PDF", "pdf", "pdf", 3),
                format("audio", "音频", "mp3,wav", "audio", 4),
                format("video", "视频", "mp4", "video", 5)
        );
    }

    private List<DictionaryItemVO> fallbackRegions() {
        DictionaryItemVO root = new DictionaryItemVO();
        root.setId(1);
        root.setCode("CN");
        root.setName("全国");
        root.setParentId(0);
        root.setLevel(0);
        root.setSort(0);
        root.setStatus(1);
        return List.of(root);
    }

    private List<DictionaryItemVO> fallbackBrowseTags() {
        return List.of(
                browseTag("sync", "同步", "core", null, null, 1),
                browseTag("quality", "精品", "core", null, null, 2),
                browseTag("free", "免费", "core", null, null, 3),
                browseTag("has_answer", "有答案", "core", null, null, 4),
                browseTag("text_version", "文字版", "core", null, null, 5),
                browseTag("exam_level", "考级", "stage", List.of("art", "dance"), null, 10),
                browseTag("competition", "竞赛", "module", null, List.of("竞赛"), 20)
        );
    }

    private static DictionaryItemVO item(String code, String name, int sort, String examLevel) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setCode(code);
        vo.setName(name);
        vo.setSort(sort);
        vo.setStatus(1);
        vo.setExamLevel(examLevel);
        return vo;
    }

    private static DictionaryItemVO scene(String code, String name, int sort) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setCode(code);
        vo.setName(name);
        vo.setSort(sort);
        vo.setStatus(1);
        return vo;
    }

    private static DictionaryItemVO format(String code, String name, String extensions, String previewType, int sort) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setCode(code);
        vo.setName(name);
        vo.setExtensions(extensions);
        vo.setPreviewType(previewType);
        vo.setSort(sort);
        vo.setStatus(1);
        return vo;
    }

    private static DictionaryItemVO browseTag(String code, String name, String group,
                                              List<String> stages, List<String> modules, int sort) {
        DictionaryItemVO vo = new DictionaryItemVO();
        vo.setCode(code);
        vo.setName(name);
        vo.setTagGroup(group);
        vo.setApplicableStages(stages != null ? stages : Collections.emptyList());
        vo.setApplicableModules(modules != null ? modules : Collections.emptyList());
        vo.setSort(sort);
        vo.setStatus(1);
        return vo;
    }
}
