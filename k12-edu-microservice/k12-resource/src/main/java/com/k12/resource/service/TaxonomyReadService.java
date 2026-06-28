package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.dto.TaxonomyEditionVO;
import com.k12.common.dto.TaxonomyGradeVO;
import com.k12.common.dto.TaxonomyModuleVO;
import com.k12.common.dto.TaxonomyResourceTypeVO;
import com.k12.common.dto.TaxonomyStageVO;
import com.k12.common.dto.TaxonomySubjectVO;
import com.k12.common.dto.TaxonomyVolumeVO;
import com.k12.common.entity.EduEdition;
import com.k12.common.entity.EduGrade;
import com.k12.common.entity.EduResourceType;
import com.k12.common.entity.EduStage;
import com.k12.common.entity.EduSubject;
import com.k12.common.entity.EduVolume;
import com.k12.resource.mapper.EduEditionMapper;
import com.k12.resource.mapper.EduGradeMapper;
import com.k12.resource.mapper.EduModuleMapper;
import com.k12.resource.mapper.EduResourceTypeMapper;
import com.k12.resource.mapper.EduStageMapper;
import com.k12.resource.mapper.EduSubjectMapper;
import com.k12.resource.mapper.EduVolumeMapper;
import com.k12.resource.util.UploadPlacementCatalog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 5-A：分类维度统一读服务（DB 优先）
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class TaxonomyReadService {

    private final EduStageMapper eduStageMapper;
    private final EduSubjectMapper eduSubjectMapper;
    private final EduEditionMapper eduEditionMapper;
    private final EduGradeMapper eduGradeMapper;
    private final EduVolumeMapper eduVolumeMapper;
    private final EduModuleMapper eduModuleMapper;
    private final EduResourceTypeMapper eduResourceTypeMapper;
    public TaxonomyReadService(EduStageMapper eduStageMapper, EduSubjectMapper eduSubjectMapper, EduEditionMapper eduEditionMapper, EduGradeMapper eduGradeMapper, EduVolumeMapper eduVolumeMapper, EduModuleMapper eduModuleMapper, EduResourceTypeMapper eduResourceTypeMapper) {
        this.eduStageMapper = eduStageMapper;
        this.eduSubjectMapper = eduSubjectMapper;
        this.eduEditionMapper = eduEditionMapper;
        this.eduGradeMapper = eduGradeMapper;
        this.eduVolumeMapper = eduVolumeMapper;
        this.eduModuleMapper = eduModuleMapper;
        this.eduResourceTypeMapper = eduResourceTypeMapper;
    }


    /**
     * 学段列表（默认仅返回启用项，按 sort 升序）
     */
    public List<TaxonomyStageVO> listStages(boolean includeDisabled) {
        LambdaQueryWrapper<EduStage> wrapper = new LambdaQueryWrapper<>();
        if (!includeDisabled) {
            wrapper.eq(EduStage::getStatus, 1);
        }
        wrapper.orderByAsc(EduStage::getSort).orderByAsc(EduStage::getId);
        List<EduStage> rows = eduStageMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            log.warn("edu_stage 无数据，返回内置学段兜底");
            return fallbackStages();
        }
        List<TaxonomyStageVO> result = new ArrayList<>(rows.size());
        for (EduStage row : rows) {
            result.add(TaxonomyStageVO.fromEntity(row));
        }
        return result;
    }

    /**
     * 学科列表（按学段过滤）
     *
     * @param stage 学段 code（primary）或中文名（小学），为空则返回全部启用学科
     */
    public List<TaxonomySubjectVO> listSubjects(String stage, boolean includeDisabled) {
        EduStage stageRow = resolveStage(stage);
        if (StringUtils.hasText(stage) && stageRow == null) {
            log.warn("未识别学段参数 stage={}", stage);
            return List.of();
        }

        LambdaQueryWrapper<EduSubject> wrapper = new LambdaQueryWrapper<>();
        if (stageRow != null) {
            wrapper.eq(EduSubject::getStageId, stageRow.getId());
        }
        if (!includeDisabled) {
            wrapper.eq(EduSubject::getStatus, 1);
        }
        wrapper.orderByAsc(EduSubject::getSort).orderByAsc(EduSubject::getId);

        List<EduSubject> rows = eduSubjectMapper.selectList(wrapper);
        Map<Integer, EduStage> stageCache = new LinkedHashMap<>();

        if (rows.isEmpty()) {
            if (stageRow != null) {
                log.warn("edu_subject 无数据 stage={}，返回内置学科兜底", stage);
                return fallbackSubjects(stageRow);
            }
            return List.of();
        }

        List<TaxonomySubjectVO> result = new ArrayList<>(rows.size());
        for (EduSubject row : rows) {
            EduStage resolvedStage = stageRow;
            if (resolvedStage == null && row.getStageId() != null) {
                resolvedStage = stageCache.computeIfAbsent(row.getStageId(), this::findStageById);
            }
            result.add(TaxonomySubjectVO.fromEntity(row, resolvedStage));
        }
        return result;
    }

    /**
     * 年级列表（按学段过滤，仅 K12 适用年级维度）
     *
     * @param stage 学段 code（primary）或中文名（小学），必填
     */
    public List<TaxonomyGradeVO> listGrades(String stage, boolean includeDisabled) {
        if (!StringUtils.hasText(stage)) {
            return List.of();
        }
        EduStage stageRow = resolveStage(stage);
        if (stageRow == null) {
            log.warn("未识别学段参数 stage={}", stage);
            return List.of();
        }

        LambdaQueryWrapper<EduGrade> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EduGrade::getStageId, stageRow.getId());
        if (!includeDisabled) {
            wrapper.eq(EduGrade::getStatus, 1);
        }
        wrapper.orderByAsc(EduGrade::getSort).orderByAsc(EduGrade::getId);
        List<EduGrade> rows = eduGradeMapper.selectList(wrapper);
        if (rows.isEmpty()) {
            log.warn("edu_grade 无数据 stage={}，返回内置年级兜底", stage);
            return fallbackGrades(stageRow);
        }
        List<TaxonomyGradeVO> result = new ArrayList<>(rows.size());
        for (EduGrade row : rows) {
            result.add(TaxonomyGradeVO.fromEntity(row, stageRow));
        }
        return result;
    }

    /**
     * 教材版本列表（按学段 + 学科过滤）
     *
     * @param stage   学段 code 或中文名（可选，建议传入以精确匹配学科）
     * @param subject 学科 code 或中文名（必填）
     */
    public List<TaxonomyEditionVO> listEditions(String stage, String subject, boolean includeDisabled) {
        if (!StringUtils.hasText(subject)) {
            return List.of();
        }
        EduSubject subjectRow = resolveSubject(stage, subject);
        if (subjectRow == null) {
            log.warn("未识别学科参数 stage={} subject={}", stage, subject);
            return List.of();
        }

        List<EduEdition> rows = eduEditionMapper.selectBySubjectId(subjectRow.getId(), includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_edition 无关联数据 subject={}，返回内置版本兜底", subject);
            return fallbackEditions(subjectRow);
        }

        List<TaxonomyEditionVO> result = new ArrayList<>(rows.size());
        for (EduEdition row : rows) {
            result.add(TaxonomyEditionVO.fromEntity(row, subjectRow));
        }
        return result;
    }

    /**
     * 教材册别列表（按学段过滤，展示名如「一年级上册」）
     *
     * @param stage 学段 code（primary）或中文名（小学），必填
     */
    public List<TaxonomyVolumeVO> listVolumes(String stage, boolean includeDisabled) {
        if (!StringUtils.hasText(stage)) {
            return List.of();
        }
        EduStage stageRow = resolveStage(stage);
        if (stageRow == null) {
            log.warn("未识别学段参数 stage={}", stage);
            return List.of();
        }

        if ("preschool".equals(stageRow.getCode())) {
            return fallbackVolumes(stageRow);
        }

        try {
            LambdaQueryWrapper<EduGrade> gradeWrapper = new LambdaQueryWrapper<>();
            gradeWrapper.eq(EduGrade::getStageId, stageRow.getId());
            if (!includeDisabled) {
                gradeWrapper.eq(EduGrade::getStatus, 1);
            }
            gradeWrapper.orderByAsc(EduGrade::getSort).orderByAsc(EduGrade::getId);
            List<EduGrade> grades = eduGradeMapper.selectList(gradeWrapper);

            LambdaQueryWrapper<EduVolume> volWrapper = new LambdaQueryWrapper<EduVolume>()
                    .in(EduVolume::getCode, List.of("up", "down"));
            if (!includeDisabled) {
                volWrapper.eq(EduVolume::getStatus, 1);
            }
            List<EduVolume> volumes = eduVolumeMapper.selectList(volWrapper
                    .orderByAsc(EduVolume::getSort)
                    .orderByAsc(EduVolume::getId));

            if (grades.isEmpty() || volumes.isEmpty()) {
                log.warn("edu_grade/edu_volume 无数据 stage={}，返回内置册别兜底", stage);
                return fallbackVolumes(stageRow);
            }

            List<TaxonomyVolumeVO> result = new ArrayList<>();
            int sort = 0;
            for (EduGrade grade : grades) {
                String displayGrade = normalizeGradeDisplayName(stageRow, grade.getName());
                for (EduVolume volume : volumes) {
                    String displayName = displayGrade + volume.getName();
                    String code = buildVolumeCode(grade.getCode(), volume.getCode());
                    result.add(TaxonomyVolumeVO.fromParts(
                            stageRow, grade, volume, displayGrade, displayName, code, ++sort));
                }
            }
            return result;
        } catch (Exception ex) {
            log.warn("listVolumes 查询失败 stage={}，返回内置册别兜底: {}", stage, ex.getMessage());
            return fallbackVolumes(stageRow);
        }
    }

    private String normalizeGradeDisplayName(EduStage stage, String gradeName) {
        if (stage == null || !StringUtils.hasText(gradeName)) {
            return gradeName;
        }
        if ("junior".equals(stage.getCode())) {
            return switch (gradeName.trim()) {
                case "初一" -> "七年级";
                case "初二" -> "八年级";
                case "初三" -> "九年级";
                default -> gradeName.trim();
            };
        }
        return gradeName.trim();
    }

    private String buildVolumeCode(String gradeCode, String volumeCode) {
        if (!StringUtils.hasText(gradeCode) || !StringUtils.hasText(volumeCode)) {
            return null;
        }
        return gradeCode.trim() + "_" + volumeCode.trim();
    }

    private List<TaxonomyVolumeVO> fallbackVolumes(EduStage stage) {
        List<VolumeSeed> seeds = FALLBACK_VOLUMES.get(stage.getCode());
        if (seeds == null) {
            List<String> catalog = UploadPlacementCatalog.volumesForStage(stage.getName());
            if (catalog.isEmpty()) {
                return List.of();
            }
            List<TaxonomyVolumeVO> list = new ArrayList<>(catalog.size());
            int sort = 0;
            for (String name : catalog) {
                list.add(fallbackVolume(stage, name, ++sort));
            }
            return list;
        }
        List<TaxonomyVolumeVO> list = new ArrayList<>(seeds.size());
        for (VolumeSeed seed : seeds) {
            TaxonomyVolumeVO vo = new TaxonomyVolumeVO();
            vo.setId(seed.id);
            vo.setCode(seed.code);
            vo.setName(seed.name);
            vo.setStageId(stage.getId());
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
            vo.setGradeName(seed.gradeName);
            vo.setVolumePart(seed.volumePart);
            vo.setSort(seed.sort);
            list.add(vo);
        }
        return list;
    }

    private TaxonomyVolumeVO fallbackVolume(EduStage stage, String name, int sort) {
        TaxonomyVolumeVO vo = new TaxonomyVolumeVO();
        vo.setId(sort);
        vo.setName(name);
        vo.setStageId(stage.getId());
        vo.setStageCode(stage.getCode());
        vo.setStageName(stage.getName());
        vo.setSort(sort);
        if (name.endsWith("上册")) {
            vo.setGradeName(name.substring(0, name.length() - 2));
            vo.setVolumePart("上册");
            vo.setVolumeCode("up");
        } else if (name.endsWith("下册")) {
            vo.setGradeName(name.substring(0, name.length() - 2));
            vo.setVolumePart("下册");
            vo.setVolumeCode("down");
        } else if (name.endsWith("上学期")) {
            vo.setGradeName(name.substring(0, name.length() - 3));
            vo.setVolumePart("上学期");
        } else if (name.endsWith("下学期")) {
            vo.setGradeName(name.substring(0, name.length() - 3));
            vo.setVolumePart("下学期");
        }
        return vo;
    }

    /**
     * 栏目列表（按学段过滤）
     */
    public List<TaxonomyModuleVO> listModules(String stage, boolean includeDisabled) {
        if (!StringUtils.hasText(stage)) {
            return List.of();
        }
        EduStage stageRow = resolveStage(stage);
        if (stageRow == null) {
            log.warn("未识别学段参数 stage={}", stage);
            return List.of();
        }

        List<Map<String, Object>> rows = eduModuleMapper.findByStageId(stageRow.getId(), includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_module 无关联数据 stage={}，返回内置栏目兜底", stage);
            return fallbackModules(stageRow);
        }

        List<TaxonomyModuleVO> result = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows) {
            result.add(TaxonomyModuleVO.fromRow(row, stageRow.getId(), stageRow.getCode(), stageRow.getName()));
        }
        return result;
    }

    /**
     * 资源类型列表（叶子节点，可按栏目缩小分组）
     */
    public List<TaxonomyResourceTypeVO> listResourceTypes(
            String stage, String subject, String module, boolean includeDisabled) {
        String groupCode = resolveResourceTypeGroup(module);
        List<EduResourceType> rows = eduResourceTypeMapper.selectLeafTypes(groupCode, includeDisabled);
        if (rows.isEmpty()) {
            log.warn("edu_resource_type 无数据 group={}，返回内置类型兜底", groupCode);
            return fallbackResourceTypes(module);
        }

        List<TaxonomyResourceTypeVO> result = new ArrayList<>(rows.size());
        for (EduResourceType row : rows) {
            result.add(TaxonomyResourceTypeVO.fromEntity(row));
        }
        return result;
    }

    /** Phase 5-F: resolve subject row for home subject-nav (includes disabled when explicitly requested elsewhere) */
    public EduSubject resolveSubjectForNav(String stage, String subject) {
        return resolveSubject(stage, subject);
    }

    /** Phase 5-F: resolve stage row for home subject-nav */
    public EduStage resolveStageForNav(String stage) {
        return resolveStage(stage);
    }

    /** Phase 5-F: load stage by id */
    public EduStage findStageById(Integer stageId) {
        return findStageByIdInternal(stageId);
    }

    private String resolveResourceTypeGroup(String module) {
        if (!StringUtils.hasText(module)) {
            return null;
        }
        String name = module.trim();
        if (name.contains("同步") || name.contains("备课") || name.contains("启蒙") || name.contains("教学")) {
            return "teach";
        }
        if (name.contains("月考") || name.contains("期中") || name.contains("期末")
                || name.contains("模拟") || name.contains("真题") || name.contains("测试")
                || name.contains("试卷") || name.contains("单元")) {
            return "practice";
        }
        if (name.contains("复习") || name.contains("专题")) {
            return "review";
        }
        if (name.contains("课本") || name.contains("教辅")) {
            return "textbook";
        }
        if (name.contains("视频") || name.contains("音频") || name.contains("微课") || name.contains("朗读")) {
            return "media";
        }
        if (name.contains("反思") || name.contains("总结")) {
            return "reflect";
        }
        if (name.contains("素材") || name.contains("纯素材")) {
            return "material";
        }
        return null;
    }

    private List<TaxonomyModuleVO> fallbackModules(EduStage stage) {
        List<ModuleSeed> seeds = FALLBACK_MODULES.get(stage.getCode());
        if (seeds == null) {
            seeds = FALLBACK_MODULES.get("default");
        }
        if (seeds == null) {
            return List.of();
        }
        TaxonomyModuleVO.EduStageRef stageRef = new TaxonomyModuleVO.EduStageRef(
                stage.getId(), stage.getCode(), stage.getName());
        List<TaxonomyModuleVO> list = new ArrayList<>(seeds.size());
        for (ModuleSeed seed : seeds) {
            list.add(TaxonomyModuleVO.fromFallback(seed.id, seed.code, seed.name, stageRef, seed.sort));
        }
        return list;
    }

    private List<TaxonomyResourceTypeVO> fallbackResourceTypes(String module) {
        String groupCode = resolveResourceTypeGroup(module);
        List<TypeSeed> seeds = FALLBACK_RESOURCE_TYPES.getOrDefault(
                groupCode == null ? "default" : groupCode,
                FALLBACK_RESOURCE_TYPES.get("default"));
        List<TaxonomyResourceTypeVO> list = new ArrayList<>(seeds.size());
        for (TypeSeed seed : seeds) {
            list.add(TaxonomyResourceTypeVO.fromFallback(seed.id, seed.code, seed.name, seed.sort));
        }
        return list;
    }

    private EduSubject resolveSubject(String stage, String subject) {
        if (!StringUtils.hasText(subject)) {
            return null;
        }
        String trimmed = subject.trim();
        EduStage stageRow = resolveStage(stage);
        LambdaQueryWrapper<EduSubject> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(EduSubject::getCode, trimmed).or().eq(EduSubject::getName, trimmed));
        if (stageRow != null) {
            wrapper.eq(EduSubject::getStageId, stageRow.getId());
        }
        wrapper.eq(EduSubject::getStatus, 1);
        wrapper.last("LIMIT 1");
        return eduSubjectMapper.selectOne(wrapper);
    }

    private EduStage resolveStage(String stage) {
        if (!StringUtils.hasText(stage)) {
            return null;
        }
        String trimmed = stage.trim();
        EduStage byCode = eduStageMapper.selectOne(new LambdaQueryWrapper<EduStage>()
                .eq(EduStage::getCode, trimmed)
                .last("LIMIT 1"));
        if (byCode != null) {
            return byCode;
        }
        return eduStageMapper.selectOne(new LambdaQueryWrapper<EduStage>()
                .eq(EduStage::getName, trimmed)
                .last("LIMIT 1"));
    }

    private EduStage findStageByIdInternal(Integer stageId) {
        if (stageId == null) {
            return null;
        }
        return eduStageMapper.selectById(stageId);
    }

    /** 与 sql/99_seed_all.sql 一致的兜底数据（仅 DB 空表时使用） */
    private List<TaxonomyStageVO> fallbackStages() {
        return List.of(
                stage(1, "primary", "小学", "🏫", 1),
                stage(2, "junior", "初中", "📚", 2),
                stage(3, "senior", "高中", "🎓", 3),
                stage(4, "art", "美术", "🎨", 4),
                stage(5, "dance", "舞蹈", "💃", 5),
                stage(6, "preschool", "幼儿", "🌱", 6)
        );
    }

    private List<TaxonomyGradeVO> fallbackGrades(EduStage stage) {
        List<GradeSeed> seeds = FALLBACK_GRADES.get(stage.getCode());
        if (seeds == null) {
            return List.of();
        }
        List<TaxonomyGradeVO> list = new ArrayList<>(seeds.size());
        for (GradeSeed seed : seeds) {
            TaxonomyGradeVO vo = new TaxonomyGradeVO();
            vo.setId(seed.id);
            vo.setStageId(stage.getId());
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
            vo.setCode(seed.code);
            vo.setName(seed.name);
            vo.setSort(seed.sort);
            vo.setStatus(1);
            list.add(vo);
        }
        return list;
    }

    private List<TaxonomySubjectVO> fallbackSubjects(EduStage stage) {
        List<SubjectSeed> seeds = FALLBACK_SUBJECTS.get(stage.getCode());
        if (seeds == null) {
            return List.of();
        }
        List<TaxonomySubjectVO> list = new ArrayList<>(seeds.size());
        for (SubjectSeed seed : seeds) {
            TaxonomySubjectVO vo = new TaxonomySubjectVO();
            vo.setId(seed.id);
            vo.setStageId(stage.getId());
            vo.setStageCode(stage.getCode());
            vo.setStageName(stage.getName());
            vo.setCode(seed.code);
            vo.setName(seed.name);
            vo.setIcon(seed.icon);
            vo.setSort(seed.sort);
            vo.setStatus(1);
            list.add(vo);
        }
        return list;
    }

    private List<TaxonomyEditionVO> fallbackEditions(EduSubject subject) {
        List<String> codes = FALLBACK_EDITION_CODES_BY_SUBJECT.get(subject.getCode());
        if (codes == null || codes.isEmpty()) {
            codes = FALLBACK_EDITION_CODES_BY_SUBJECT.get("default");
        }
        if (codes == null) {
            return List.of();
        }
        List<TaxonomyEditionVO> list = new ArrayList<>();
        int sort = 0;
        for (String code : codes) {
            EditionSeed seed = FALLBACK_EDITIONS.get(code);
            if (seed == null) {
                continue;
            }
            TaxonomyEditionVO vo = new TaxonomyEditionVO();
            vo.setId(seed.id);
            vo.setCode(seed.code);
            vo.setName(seed.name);
            vo.setShortName(seed.shortName);
            vo.setSort(++sort);
            vo.setStatus(1);
            vo.setSubjectId(subject.getId());
            vo.setSubjectCode(subject.getCode());
            vo.setSubjectName(subject.getName());
            list.add(vo);
        }
        return list;
    }

    private TaxonomyStageVO stage(int id, String code, String name, String icon, int sort) {
        TaxonomyStageVO vo = new TaxonomyStageVO();
        vo.setId(id);
        vo.setCode(code);
        vo.setName(name);
        vo.setIcon(icon);
        vo.setSort(sort);
        vo.setStatus(1);
        return vo;
    }

    private record GradeSeed(int id, String code, String name, int sort) {}

    private record SubjectSeed(int id, String code, String name, String icon, int sort) {}

    private record EditionSeed(int id, String code, String name, String shortName) {}

    private record VolumeSeed(int id, String code, String name, String gradeName, String volumePart, int sort) {}

    private record ModuleSeed(int id, String code, String name, int sort) {}

    private record TypeSeed(int id, String code, String name, int sort) {}

    private static final Map<String, EditionSeed> FALLBACK_EDITIONS = Map.ofEntries(
            Map.entry("tongbian2024", edition(1, "tongbian2024", "统编版(2024)", "统编")),
            Map.entry("renjiao", edition(2, "renjiao", "人教版", "人教")),
            Map.entry("beishida", edition(3, "beishida", "北师大版", "北师大")),
            Map.entry("sujiao", edition(4, "sujiao", "苏教版", "苏教")),
            Map.entry("hujiao", edition(5, "hujiao", "沪教版", "沪教")),
            Map.entry("xishida", edition(6, "xishida", "西师大版", "西师大")),
            Map.entry("yuwen", edition(7, "yuwen", "语文版", "语文版")),
            Map.entry("jijiao", edition(8, "jijiao", "冀教版", "冀教")),
            Map.entry("tongbian2016", edition(9, "tongbian2016", "统编版(2016)", "统编16"))
    );

    private static final List<String> EDITION_ALL = List.of(
            "tongbian2024", "renjiao", "beishida", "sujiao", "hujiao", "xishida", "yuwen", "jijiao", "tongbian2016");
    private static final List<String> EDITION_NO_YUWEN = List.of(
            "tongbian2024", "renjiao", "beishida", "sujiao", "hujiao", "xishida", "jijiao", "tongbian2016");
    private static final List<String> EDITION_MAIN5 = List.of(
            "tongbian2024", "renjiao", "beishida", "sujiao", "hujiao");
    private static final List<String> EDITION_TONGBIAN = List.of("tongbian2024", "tongbian2016", "renjiao");
    private static final List<String> EDITION_HISTORY = List.of("tongbian2024", "tongbian2016", "renjiao", "beishida");
    private static final List<String> EDITION_ART_PE = List.of("tongbian2024", "renjiao", "beishida", "sujiao");

    private static final Map<String, List<String>> FALLBACK_EDITION_CODES_BY_SUBJECT = Map.ofEntries(
            Map.entry("chinese", EDITION_ALL),
            Map.entry("math", EDITION_NO_YUWEN),
            Map.entry("english", EDITION_NO_YUWEN),
            Map.entry("science", EDITION_MAIN5),
            Map.entry("politics", EDITION_TONGBIAN),
            Map.entry("physics", EDITION_MAIN5),
            Map.entry("chemistry", EDITION_MAIN5),
            Map.entry("biology", EDITION_MAIN5),
            Map.entry("history", EDITION_HISTORY),
            Map.entry("geography", EDITION_MAIN5),
            Map.entry("music", EDITION_ART_PE),
            Map.entry("art", EDITION_ART_PE),
            Map.entry("pe", EDITION_ART_PE),
            Map.entry("habit", List.of("tongbian2024", "renjiao")),
            Map.entry("activity", List.of("tongbian2024", "renjiao", "beishida")),
            Map.entry("dance", EDITION_ART_PE),
            Map.entry("default", EDITION_ALL)
    );

    private static EditionSeed edition(int id, String code, String name, String shortName) {
        return new EditionSeed(id, code, name, shortName);
    }

    private static final Map<String, List<GradeSeed>> FALLBACK_GRADES = Map.ofEntries(
            Map.entry("primary", List.of(
                    grade(1, "grade1", "一年级", 1),
                    grade(2, "grade2", "二年级", 2),
                    grade(3, "grade3", "三年级", 3),
                    grade(4, "grade4", "四年级", 4),
                    grade(5, "grade5", "五年级", 5),
                    grade(6, "grade6", "六年级", 6))),
            Map.entry("junior", List.of(
                    grade(7, "grade7", "七年级", 1),
                    grade(8, "grade8", "八年级", 2),
                    grade(9, "grade9", "九年级", 3))),
            Map.entry("senior", List.of(
                    grade(10, "grade10", "高一", 1),
                    grade(11, "grade11", "高二", 2),
                    grade(12, "grade12", "高三", 3)))
    );

    private static GradeSeed grade(int id, String code, String name, int sort) {
        return new GradeSeed(id, code, name, sort);
    }

    private static final Map<String, List<SubjectSeed>> FALLBACK_SUBJECTS = Map.ofEntries(
            Map.entry("primary", List.of(
                    subject(1, "chinese", "语文", "📖", 1),
                    subject(2, "math", "数学", "🔢", 2),
                    subject(3, "english", "英语", "🌍", 3),
                    subject(4, "science", "科学", "🔬", 4),
                    subject(5, "politics", "道德与法治", "🏛️", 5),
                    subject(6, "music", "音乐", "🎵", 6),
                    subject(7, "art", "美术", "🎨", 7),
                    subject(8, "pe", "体育", "⚽", 8))),
            Map.entry("junior", List.of(
                    subject(9, "chinese", "语文", "📖", 1),
                    subject(10, "math", "数学", "🔢", 2),
                    subject(11, "english", "英语", "🌍", 3),
                    subject(12, "physics", "物理", "⚡", 4),
                    subject(13, "chemistry", "化学", "🧪", 5),
                    subject(14, "biology", "生物", "🌱", 6),
                    subject(15, "history", "历史", "📜", 7),
                    subject(16, "geography", "地理", "🗺️", 8),
                    subject(17, "politics", "政治", "🏛️", 9))),
            Map.entry("senior", List.of(
                    subject(18, "chinese", "语文", "📖", 1),
                    subject(19, "math", "数学", "🔢", 2),
                    subject(20, "english", "英语", "🌍", 3),
                    subject(21, "physics", "物理", "⚡", 4),
                    subject(22, "chemistry", "化学", "🧪", 5),
                    subject(23, "biology", "生物", "🌱", 6),
                    subject(24, "history", "历史", "📜", 7),
                    subject(25, "geography", "地理", "🗺️", 8),
                    subject(26, "politics", "政治", "🏛️", 9))),
            Map.entry("art", List.of(subject(27, "art", "美术", "🎨", 1))),
            Map.entry("dance", List.of(subject(28, "dance", "舞蹈", "💃", 1))),
            Map.entry("preschool", List.of(
                    subject(29, "chinese", "拼音识字", "🔤", 1),
                    subject(30, "math", "数学启蒙", "🔢", 2),
                    subject(31, "habit", "习惯养成", "🌟", 3),
                    subject(32, "activity", "综合活动", "🧩", 4)))
    );

    private static SubjectSeed subject(int id, String code, String name, String icon, int sort) {
        return new SubjectSeed(id, code, name, icon, sort);
    }

    /** 与前端 volumeDataMap 一致的兜底册别 */
    private static final Map<String, List<VolumeSeed>> FALLBACK_VOLUMES = Map.ofEntries(
            Map.entry("preschool", List.of(
                    volume(1, "k2s1", "中班上学期", "中班", "上学期", 1),
                    volume(2, "k2s2", "中班下学期", "中班", "下学期", 2),
                    volume(3, "k3s1", "大班上学期", "大班", "上学期", 3),
                    volume(4, "k3s2", "大班下学期", "大班", "下学期", 4),
                    volume(5, "bridge_summer", "暑假衔接", "幼小衔接", null, 5))),
            Map.entry("senior", List.of(
                    volume(10, "s10s1", "必修一", "必修一", null, 1),
                    volume(11, "s10s2", "必修二", "必修二", null, 2),
                    volume(12, "s11s1", "选择性必修一", "选择性必修一", null, 3),
                    volume(13, "s11s2", "选择性必修二", "选择性必修二", null, 4)))
    );

    private static VolumeSeed volume(int id, String code, String name, String gradeName, String volumePart, int sort) {
        return new VolumeSeed(id, code, name, gradeName, volumePart, sort);
    }

    private static ModuleSeed module(int id, String code, String name, int sort) {
        return new ModuleSeed(id, code, name, sort);
    }

    private static TypeSeed type(int id, String code, String name, int sort) {
        return new TypeSeed(id, code, name, sort);
    }

    private static final Map<String, List<ModuleSeed>> FALLBACK_MODULES = Map.ofEntries(
            Map.entry("primary", List.of(
                    module(1, "sync_prep", "同步备课", 1),
                    module(3, "monthly", "月考", 2),
                    module(4, "midterm", "期中", 3),
                    module(5, "final", "期末", 4),
                    module(10, "topic_review", "专题复习", 5),
                    module(7, "xsc_real", "小升初真题", 6),
                    module(8, "xsc_mock", "小升初模拟", 7))),
            Map.entry("junior", List.of(
                    module(1, "sync_prep", "同步备课", 1),
                    module(19, "round1", "一轮复习", 2),
                    module(20, "round2", "二轮专题", 3),
                    module(21, "round3", "三轮冲刺", 4),
                    module(3, "monthly", "月考", 5),
                    module(4, "midterm", "期中", 6),
                    module(5, "final", "期末", 7),
                    module(22, "zk_mock", "中考模拟", 8),
                    module(23, "zk_real", "中考真题", 9))),
            Map.entry("senior", List.of(
                    module(1, "sync_prep", "同步备课", 1),
                    module(19, "round1", "一轮复习", 2),
                    module(20, "round2", "二轮专题", 3),
                    module(21, "round3", "三轮冲刺", 4),
                    module(3, "monthly", "月考", 5),
                    module(4, "midterm", "期中", 6),
                    module(5, "final", "期末", 7),
                    module(24, "gk_mock", "高考模拟", 8),
                    module(25, "gk_real", "高考真题", 9))),
            Map.entry("preschool", List.of(
                    module(34, "preschool_pinyin", "拼音识字", 1),
                    module(35, "preschool_math", "数学启蒙", 2),
                    module(37, "preschool_habit", "习惯养成", 3),
                    module(42, "preschool_activity", "综合活动", 4),
                    module(39, "preschool_bridge", "幼小衔接", 5),
                    module(38, "preschool_summer", "暑假衔接", 6))),
            Map.entry("default", List.of(
                    module(1, "sync_prep", "同步备课", 1),
                    module(19, "round1", "一轮复习", 2),
                    module(10, "topic_review", "专题复习", 3),
                    module(3, "monthly", "月考", 4),
                    module(4, "midterm", "期中", 5),
                    module(5, "final", "期末", 6)))
    );

    private static final Map<String, List<TypeSeed>> FALLBACK_RESOURCE_TYPES = Map.ofEntries(
            Map.entry("teach", List.of(
                    type(13, "courseware", "课件", 1),
                    type(11, "lesson_plan", "教案", 2),
                    type(12, "study_guide", "学案", 3),
                    type(25, "practice", "练习", 4),
                    type(23, "exam_paper", "试卷", 5),
                    type(55, "video", "视频", 6),
                    type(79, "knowledge_point", "知识点", 7),
                    type(80, "other", "其他", 99))),
            Map.entry("practice", List.of(
                    type(23, "exam_paper", "试卷", 1),
                    type(25, "practice", "练习", 2),
                    type(22, "unit_test", "单元测试", 3),
                    type(24, "answer_analysis", "答案解析", 4))),
            Map.entry("review", List.of(
                    type(32, "topic_lecture", "专题讲义", 1),
                    type(23, "exam_paper", "试卷", 2),
                    type(12, "study_guide", "学案", 3),
                    type(13, "courseware", "课件", 4),
                    type(31, "review_outline", "复习提纲", 5))),
            Map.entry("default", List.of(
                    type(13, "courseware", "课件", 1),
                    type(11, "lesson_plan", "教案", 2),
                    type(25, "practice", "练习", 3),
                    type(23, "exam_paper", "试卷", 4),
                    type(12, "study_guide", "学案", 5),
                    type(55, "video", "视频", 6),
                    type(79, "knowledge_point", "知识点", 7),
                    type(80, "other", "其他", 99)))
    );
}
