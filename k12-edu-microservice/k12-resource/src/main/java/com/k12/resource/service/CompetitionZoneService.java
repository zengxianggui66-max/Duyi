package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.CompetitionQueryDTO;
import com.k12.common.dto.CompetitionResourceUploadDTO;
import com.k12.common.entity.CompetitionPackageItem;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CompetitionTrainingPackage;
import com.k12.resource.mapper.CompetitionPackageItemMapper;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CompetitionTrainingPackageMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CompetitionZoneService {

    private static final Map<String, String> CATEGORY_ICONS = Map.ofEntries(
            Map.entry("olympiad", "🔬"),
            Map.entry("primary_math", "📐"),
            Map.entry("math", "🔢"),
            Map.entry("physics", "⚡"),
            Map.entry("chemistry", "🧪"),
            Map.entry("biology", "🧬"),
            Map.entry("info", "💻"),
            Map.entry("writing", "✍️"),
            Map.entry("english", "📝"),
            Map.entry("cert", "📜"),
            Map.entry("exam_prep", "🎯")
    );

    private final CompetitionResourceMapper competitionResourceMapper;
    private final CompetitionTrainingPackageMapper packageMapper;
    private final CompetitionPackageItemMapper packageItemMapper;
    private final FileService fileService;
    private final SearchIndexSyncHook searchIndexSyncHook;
    public CompetitionZoneService(CompetitionResourceMapper competitionResourceMapper, CompetitionTrainingPackageMapper packageMapper, CompetitionPackageItemMapper packageItemMapper, FileService fileService, SearchIndexSyncHook searchIndexSyncHook) {
        this.competitionResourceMapper = competitionResourceMapper;
        this.packageMapper = packageMapper;
        this.packageItemMapper = packageItemMapper;
        this.fileService = fileService;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    public Map<String, Object> listResourcesByPage(CompetitionQueryDTO dto) {
        Page<CompetitionResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<CompetitionResource> w = buildResourceWrapper(dto);
        applyResourceSort(w, dto);
        IPage<CompetitionResource> result = competitionResourceMapper.selectPage(page, w);
        return pageResult(result);
    }

    public CompetitionResource getResource(Long id) {
        CompetitionResource r = competitionResourceMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(404, "资源不存在");
        }
        return r;
    }

    public void incrementResourceView(Long id) {
        CompetitionResource r = getResource(id);
        r.setViewCount((r.getViewCount() != null ? r.getViewCount() : 0) + 1);
        competitionResourceMapper.updateById(r);
    }

    public void incrementResourceDownload(Long id) {
        CompetitionResource r = getResource(id);
        r.setDownloadCount((r.getDownloadCount() != null ? r.getDownloadCount() : 0) + 1);
        competitionResourceMapper.updateById(r);
    }

    public Map<String, Object> listPackagesByPage(CompetitionQueryDTO dto) {
        Page<CompetitionTrainingPackage> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<CompetitionTrainingPackage> w = buildPackageWrapper(dto);
        w.orderByDesc(CompetitionTrainingPackage::getSort)
                .orderByDesc(CompetitionTrainingPackage::getDownloadCount);
        IPage<CompetitionTrainingPackage> result = packageMapper.selectPage(page, w);
        return pageResult(result);
    }

    @SuppressWarnings("deprecation")
    public Map<String, Object> getPackageDetail(Long id) {
        CompetitionTrainingPackage pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "精品包不存在");
        }
        List<CompetitionPackageItem> items = packageItemMapper.selectList(
                new LambdaQueryWrapper<CompetitionPackageItem>()
                        .eq(CompetitionPackageItem::getPackageId, id)
                        .orderByAsc(CompetitionPackageItem::getSort));
        List<Long> resIds = items.stream().map(CompetitionPackageItem::getResourceId).toList();
        List<CompetitionResource> resources = resIds.isEmpty() ? List.of()
                : competitionResourceMapper.selectBatchIds(resIds);
        Map<Long, CompetitionResource> resMap = resources.stream()
                .collect(Collectors.toMap(CompetitionResource::getId, r -> r, (a, b) -> a));
        List<CompetitionResource> ordered = resIds.stream()
                .map(resMap::get)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("package", pkg);
        data.put("resources", ordered);
        return data;
    }

    public void incrementPackageDownload(Long id) {
        CompetitionTrainingPackage pkg = packageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "精品包不存在");
        }
        pkg.setDownloadCount((pkg.getDownloadCount() != null ? pkg.getDownloadCount() : 0) + 1);
        packageMapper.updateById(pkg);
    }

    @Transactional
    public CompetitionResource createResource(MultipartFile file, CompetitionResourceUploadDTO dto) {
        validateUpload(dto);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传资源文件");
        }

        CompetitionResource resource = new CompetitionResource();
        resource.setTitle(dto.getTitle().trim());
        resource.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        resource.setCategory(dto.getCategory());
        resource.setGradeStage(StringUtils.hasText(dto.getGradeStage()) ? dto.getGradeStage() : "all");
        resource.setSubject(StringUtils.hasText(dto.getSubject()) ? dto.getSubject() : "");
        resource.setResourceForm(dto.getResourceForm());
        resource.setCompetitionName(StringUtils.hasText(dto.getCompetitionName()) ? dto.getCompetitionName().trim() : "");
        String tags = StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : "";
        if (!tags.contains("学科竞赛")) {
            tags = tags.isEmpty() ? "学科竞赛" : "学科竞赛," + tags;
        }
        resource.setTags(tags);
        resource.setIcon(resolveIcon(dto));
        resource.setIsFree(dto.getIsFree() != null ? dto.getIsFree() : 1);
        resource.setIsElite(0);
        resource.setStatus(1);
        resource.setSort(0);
        resource.setDownloadCount(0);
        resource.setViewCount(0);

        try {
            Map<String, Object> uploadResult = fileService.uploadFile(file);
            resource.setFileUrl((String) uploadResult.get("fileUrl"));
            String ext = (String) uploadResult.get("fileFormat");
            resource.setFileFormat(ext != null ? ext.toUpperCase() : "FILE");
        } catch (Exception e) {
            throw new BusinessException(500, "文件上传失败: " + e.getMessage());
        }

        competitionResourceMapper.insert(resource);
        searchIndexSyncHook.afterCompetitionChanged(resource.getId());
        return resource;
    }

    private void validateUpload(CompetitionResourceUploadDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(400, "请填写资源标题");
        }
        if (dto.getTitle().trim().length() > 200) {
            throw new BusinessException(400, "标题不能超过200字");
        }
        if (!StringUtils.hasText(dto.getCategory())) {
            throw new BusinessException(400, "请选择竞赛类型");
        }
        if (!StringUtils.hasText(dto.getResourceForm())) {
            throw new BusinessException(400, "请选择资源形态");
        }
    }

    private String resolveIcon(CompetitionResourceUploadDTO dto) {
        if (StringUtils.hasText(dto.getIcon())) {
            return dto.getIcon().trim();
        }
        return CATEGORY_ICONS.getOrDefault(dto.getCategory(), "🏆");
    }

    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("categories", List.of(
                Map.of("key", "all", "name", "全部", "icon", "📋"),
                Map.of("key", "olympiad", "name", "学科奥赛", "icon", "🔬"),
                Map.of("key", "primary_math", "name", "小学奥数", "icon", "📐"),
                Map.of("key", "math", "name", "数学竞赛", "icon", "🔢"),
                Map.of("key", "physics", "name", "物理竞赛", "icon", "⚡"),
                Map.of("key", "chemistry", "name", "化学竞赛", "icon", "🧪"),
                Map.of("key", "biology", "name", "生物竞赛", "icon", "🧬"),
                Map.of("key", "info", "name", "信息学", "icon", "💻"),
                Map.of("key", "writing", "name", "作文大赛", "icon", "✍️"),
                Map.of("key", "english", "name", "英语竞赛", "icon", "📝"),
                Map.of("key", "cert", "name", "考级证书", "icon", "📜"),
                Map.of("key", "exam_prep", "name", "考前辅导", "icon", "🎯")
        ));
        options.put("gradeStages", List.of(
                Map.of("key", "all", "name", "不限学段"),
                Map.of("key", "primary", "name", "小学"),
                Map.of("key", "junior", "name", "初中"),
                Map.of("key", "senior", "name", "高中")
        ));
        options.put("resourceForms", List.of(
                Map.of("key", "all", "name", "不限形态"),
                Map.of("key", "exam", "name", "真题试卷"),
                Map.of("key", "mock", "name", "模拟卷"),
                Map.of("key", "lecture", "name", "讲义"),
                Map.of("key", "lesson_plan", "name", "教案"),
                Map.of("key", "ppt", "name", "课件PPT"),
                Map.of("key", "video", "name", "视频"),
                Map.of("key", "doc", "name", "文档"),
                Map.of("key", "exercise", "name", "练习题")
        ));
        return options;
    }

    private LambdaQueryWrapper<CompetitionResource> buildResourceWrapper(CompetitionQueryDTO dto) {
        LambdaQueryWrapper<CompetitionResource> w = new LambdaQueryWrapper<>();
        w.eq(CompetitionResource::getStatus, 1);
        w.and(q -> q.isNull(CompetitionResource::getDeleted).or().eq(CompetitionResource::getDeleted, 0));
        if (StringUtils.hasText(dto.getCategory()) && !"all".equals(dto.getCategory()) && !"elite".equals(dto.getCategory())) {
            w.eq(CompetitionResource::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getGradeStage()) && !"all".equals(dto.getGradeStage())) {
            w.and(q -> q.eq(CompetitionResource::getGradeStage, dto.getGradeStage())
                    .or().eq(CompetitionResource::getGradeStage, "all"));
        }
        if (StringUtils.hasText(dto.getSubject()) && !"all".equals(dto.getSubject())) {
            if ("info".equals(dto.getSubject())) {
                w.and(q -> q.eq(CompetitionResource::getCategory, "info")
                        .or().like(CompetitionResource::getTags, "信息学")
                        .or().like(CompetitionResource::getTags, "NOIP")
                        .or().like(CompetitionResource::getTags, "CSP"));
            } else {
                w.eq(CompetitionResource::getSubject, dto.getSubject());
            }
        }
        if (StringUtils.hasText(dto.getResourceForm()) && !"all".equals(dto.getResourceForm())) {
            w.eq(CompetitionResource::getResourceForm, dto.getResourceForm());
        }
        if (dto.getIsFree() != null) {
            w.eq(CompetitionResource::getIsFree, dto.getIsFree());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(CompetitionResource::getTitle, kw)
                    .or().like(CompetitionResource::getSummary, kw)
                    .or().like(CompetitionResource::getTags, kw)
                    .or().like(CompetitionResource::getCompetitionName, kw));
        }
        return w;
    }

    private LambdaQueryWrapper<CompetitionTrainingPackage> buildPackageWrapper(CompetitionQueryDTO dto) {
        LambdaQueryWrapper<CompetitionTrainingPackage> w = new LambdaQueryWrapper<>();
        w.eq(CompetitionTrainingPackage::getStatus, 1);
        if (StringUtils.hasText(dto.getCategory()) && !"all".equals(dto.getCategory())) {
            w.eq(CompetitionTrainingPackage::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getGradeStage()) && !"all".equals(dto.getGradeStage())) {
            w.and(q -> q.eq(CompetitionTrainingPackage::getGradeStage, dto.getGradeStage())
                    .or().eq(CompetitionTrainingPackage::getGradeStage, "all"));
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(CompetitionTrainingPackage::getTitle, kw)
                    .or().like(CompetitionTrainingPackage::getSummary, kw)
                    .or().like(CompetitionTrainingPackage::getTags, kw));
        }
        return w;
    }

    private void applyResourceSort(LambdaQueryWrapper<CompetitionResource> w, CompetitionQueryDTO dto) {
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        String field = dto.getSortField();
        if ("downloadCount".equals(field)) {
            w.orderBy(true, asc, CompetitionResource::getDownloadCount);
        } else if ("viewCount".equals(field)) {
            w.orderBy(true, asc, CompetitionResource::getViewCount);
        } else {
            w.orderBy(true, asc, CompetitionResource::getSort)
                    .orderByDesc(CompetitionResource::getDownloadCount);
        }
    }

    private Map<String, Object> pageResult(IPage<?> result) {
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        map.put("pages", result.getPages());
        return map;
    }
}
