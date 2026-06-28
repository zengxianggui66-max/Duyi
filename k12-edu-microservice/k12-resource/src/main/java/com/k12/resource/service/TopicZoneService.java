package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.TopicQueryDTO;
import com.k12.common.dto.TopicResourceUploadDTO;
import com.k12.common.entity.TopicAlbum;
import com.k12.common.entity.TopicAlbumItem;
import com.k12.common.entity.TopicResource;
import com.k12.resource.mapper.TopicAlbumItemMapper;
import com.k12.resource.mapper.TopicAlbumMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.Month;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TopicZoneService {

    private static final Map<String, String> CATEGORY_ICONS = Map.ofEntries(
            Map.entry("holiday_hw", "🏖️"),
            Map.entry("term_open", "🎒"),
            Map.entry("midterm_final", "📝"),
            Map.entry("promotion", "🎯"),
            Map.entry("news", "📰"),
            Map.entry("cross", "🔗"),
            Map.entry("project", "📋")
    );

    private static final Map<String, String> REGION_NAMES = Map.of(
            "chengdu", "成都",
            "mianyang", "绵阳",
            "sichuan", "四川",
            "all", "全省"
    );

    private final TopicResourceMapper topicResourceMapper;
    private final TopicAlbumMapper albumMapper;
    private final TopicAlbumItemMapper albumItemMapper;
    private final FileService fileService;
    private final SearchIndexSyncHook searchIndexSyncHook;
    public TopicZoneService(TopicResourceMapper topicResourceMapper, TopicAlbumMapper albumMapper, TopicAlbumItemMapper albumItemMapper, FileService fileService, SearchIndexSyncHook searchIndexSyncHook) {
        this.topicResourceMapper = topicResourceMapper;
        this.albumMapper = albumMapper;
        this.albumItemMapper = albumItemMapper;
        this.fileService = fileService;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    public Map<String, Object> listResourcesByPage(TopicQueryDTO dto) {
        Page<TopicResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<TopicResource> w = buildResourceWrapper(dto);
        applyResourceSort(w, dto);
        IPage<TopicResource> result = topicResourceMapper.selectPage(page, w);
        return pageResult(result);
    }

    public TopicResource getResource(Long id) {
        TopicResource r = topicResourceMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(404, "资源不存在");
        }
        return r;
    }

    public void incrementResourceView(Long id) {
        TopicResource r = getResource(id);
        r.setViewCount((r.getViewCount() != null ? r.getViewCount() : 0) + 1);
        topicResourceMapper.updateById(r);
    }

    public void incrementResourceDownload(Long id) {
        TopicResource r = getResource(id);
        r.setDownloadCount((r.getDownloadCount() != null ? r.getDownloadCount() : 0) + 1);
        topicResourceMapper.updateById(r);
    }

    public Map<String, Object> listAlbumsByPage(TopicQueryDTO dto) {
        Page<TopicAlbum> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<TopicAlbum> w = buildAlbumWrapper(dto);
        w.orderByDesc(TopicAlbum::getSort).orderByDesc(TopicAlbum::getDownloadCount);
        IPage<TopicAlbum> result = albumMapper.selectPage(page, w);
        return pageResult(result);
    }

    @SuppressWarnings("deprecation")
    public Map<String, Object> getAlbumDetail(Long id) {
        TopicAlbum album = albumMapper.selectById(id);
        if (album == null) {
            throw new BusinessException(404, "精品专辑不存在");
        }
        List<TopicAlbumItem> items = albumItemMapper.selectList(
                new LambdaQueryWrapper<TopicAlbumItem>()
                        .eq(TopicAlbumItem::getAlbumId, id)
                        .orderByAsc(TopicAlbumItem::getSort));
        List<Long> resIds = items.stream().map(TopicAlbumItem::getResourceId).toList();
        List<TopicResource> resources = resIds.isEmpty() ? List.of()
                : topicResourceMapper.selectBatchIds(resIds);
        Map<Long, TopicResource> resMap = resources.stream()
                .collect(Collectors.toMap(TopicResource::getId, r -> r, (a, b) -> a));
        List<TopicResource> ordered = resIds.stream().map(resMap::get).filter(Objects::nonNull).toList();

        Map<String, Object> data = new HashMap<>();
        data.put("album", album);
        data.put("resources", ordered);
        return data;
    }

    public void incrementAlbumDownload(Long id) {
        TopicAlbum album = albumMapper.selectById(id);
        if (album == null) {
            throw new BusinessException(404, "精品专辑不存在");
        }
        album.setDownloadCount((album.getDownloadCount() != null ? album.getDownloadCount() : 0) + 1);
        albumMapper.updateById(album);
    }

    @Transactional
    public TopicResource createResource(MultipartFile file, TopicResourceUploadDTO dto) {
        validateUpload(dto);
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "请上传资源文件");
        }

        TopicResource resource = new TopicResource();
        resource.setTitle(dto.getTitle().trim());
        resource.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        resource.setCategory(dto.getCategory());
        resource.setRegion(StringUtils.hasText(dto.getRegion()) ? dto.getRegion() : "all");
        resource.setGradeStage(StringUtils.hasText(dto.getGradeStage()) ? dto.getGradeStage() : "all");
        resource.setSubject(StringUtils.hasText(dto.getSubject()) ? dto.getSubject() : "");
        resource.setResourceForm(dto.getResourceForm());
        resource.setTopicLabel(StringUtils.hasText(dto.getTopicLabel()) ? dto.getTopicLabel().trim() : "");
        resource.setSchoolYear(StringUtils.hasText(dto.getSchoolYear()) ? dto.getSchoolYear().trim() : "");
        String tags = StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : "";
        if (!tags.contains("专题资源")) {
            tags = tags.isEmpty() ? "专题资源" : "专题资源," + tags;
        }
        String regionName = REGION_NAMES.get(resource.getRegion());
        if (regionName != null && !"全省".equals(regionName) && !tags.contains(regionName)) {
            tags = tags + "," + regionName;
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

        topicResourceMapper.insert(resource);
        searchIndexSyncHook.afterTopicChanged(resource.getId());
        return resource;
    }

    public Map<String, Object> getZoneStats() {
        LambdaQueryWrapper<TopicResource> base = new LambdaQueryWrapper<>();
        base.eq(TopicResource::getStatus, 1);
        long total = topicResourceMapper.selectCount(base);

        LambdaQueryWrapper<TopicResource> freeW = new LambdaQueryWrapper<>();
        freeW.eq(TopicResource::getStatus, 1).eq(TopicResource::getIsFree, 1);
        long free = topicResourceMapper.selectCount(freeW);

        LambdaQueryWrapper<TopicResource> eliteW = new LambdaQueryWrapper<>();
        eliteW.eq(TopicResource::getStatus, 1).eq(TopicResource::getIsElite, 1);
        long elite = topicResourceMapper.selectCount(eliteW);

        LambdaQueryWrapper<TopicAlbum> albumW = new LambdaQueryWrapper<>();
        albumW.eq(TopicAlbum::getStatus, 1);
        long albumCount = albumMapper.selectCount(albumW);

        List<Map<String, Object>> byRegion = List.of("chengdu", "mianyang", "sichuan", "all").stream()
                .map(region -> {
                    LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
                    w.eq(TopicResource::getStatus, 1).eq(TopicResource::getRegion, region);
                    long count = topicResourceMapper.selectCount(w);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("region", region);
                    m.put("name", REGION_NAMES.getOrDefault(region, region));
                    m.put("count", count);
                    return m;
                })
                .filter(m -> (Long) m.get("count") > 0)
                .toList();

        List<String> categories = List.of("holiday_hw", "term_open", "midterm_final", "promotion", "news", "cross", "project");
        List<Map<String, Object>> byCategory = categories.stream()
                .map(cat -> {
                    LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
                    w.eq(TopicResource::getStatus, 1).eq(TopicResource::getCategory, cat);
                    long count = topicResourceMapper.selectCount(w);
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("category", cat);
                    m.put("name", categoryDisplayName(cat));
                    m.put("icon", CATEGORY_ICONS.getOrDefault(cat, "📚"));
                    m.put("count", count);
                    return m;
                })
                .filter(m -> (Long) m.get("count") > 0)
                .toList();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", total);
        stats.put("free", free);
        stats.put("elite", elite);
        stats.put("albums", albumCount);
        stats.put("byRegion", byRegion);
        stats.put("byCategory", byCategory);
        return stats;
    }

    public Map<String, Object> getCalendarHint() {
        Month month = LocalDate.now().getMonth();
        int m = month.getValue();
        String category;
        String title;
        String desc;
        String icon;
        if (m >= 7 && m <= 8) {
            category = "holiday_hw";
            title = "暑假作业季";
            desc = "推荐浏览寒暑假作业、衔接练习与阅读包";
            icon = "🏖️";
        } else if (m == 9 || m == 2) {
            category = "term_open";
            title = m == 9 ? "秋季开学备考" : "春季开学备考";
            desc = "收心教育、开学摸底与第一周备课资源";
            icon = "🎒";
        } else if (m == 10 || m == 11 || m == 4 || m == 5) {
            category = "midterm_final";
            title = m <= 5 ? "期中复习冲刺" : "期末复习冲刺";
            desc = "期中/期末模拟卷、知识点清单与讲评课件";
            icon = "📝";
        } else if (m == 6) {
            category = "promotion";
            title = "升学冲刺季";
            desc = "小升初、中考、高考冲刺与真题汇编";
            icon = "🎯";
        } else if (m == 1) {
            category = "holiday_hw";
            title = "寒假作业季";
            desc = "寒假作业精选、假期阅读与收心计划";
            icon = "❄️";
        } else {
            category = "news";
            title = "时事与跨学科";
            desc = "热点时政、PBL 与跨学科融合专题";
            icon = "📰";
        }
        String schoolYear = resolveSchoolYear();
        Map<String, Object> hint = new LinkedHashMap<>();
        hint.put("category", category);
        hint.put("title", title);
        hint.put("desc", desc);
        hint.put("icon", icon);
        hint.put("schoolYear", schoolYear);
        hint.put("month", m);
        return hint;
    }

    public List<TopicResource> listHotResources(Integer limit, String region) {
        TopicQueryDTO dto = new TopicQueryDTO();
        dto.setRegion(region);
        dto.setCurrent(1);
        dto.setSize(limit != null ? limit : 8);
        dto.setSortField("downloadCount");
        dto.setSortOrder("desc");
        Page<TopicResource> page = new Page<>(1, dto.getSize());
        LambdaQueryWrapper<TopicResource> w = buildResourceWrapper(dto);
        applyResourceSort(w, dto);
        return topicResourceMapper.selectPage(page, w).getRecords();
    }

    public List<TopicResource> listLatestResources(Integer limit, String region) {
        TopicQueryDTO dto = new TopicQueryDTO();
        dto.setRegion(region);
        dto.setCurrent(1);
        dto.setSize(limit != null ? limit : 8);
        dto.setSortField("createTime");
        dto.setSortOrder("desc");
        Page<TopicResource> page = new Page<>(1, dto.getSize());
        LambdaQueryWrapper<TopicResource> w = buildResourceWrapper(dto);
        applyResourceSort(w, dto);
        return topicResourceMapper.selectPage(page, w).getRecords();
    }

    public List<Map<String, Object>> getHotKeywords(Integer limit) {
        int size = limit != null ? limit : 10;
        List<String> defaults = List.of(
                "暑假作业", "期末复习", "成都中考", "绵阳中考", "开学备考",
                "小升初", "时事热点", "PBL", "寒假作业", "期中期末"
        );
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(size, defaults.size()); i++) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("keyword", defaults.get(i));
            item.put("rank", i + 1);
            result.add(item);
        }
        LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
        w.eq(TopicResource::getStatus, 1);
        w.orderByDesc(TopicResource::getDownloadCount);
        w.last("LIMIT 3");
        List<TopicResource> top = topicResourceMapper.selectList(w);
        for (TopicResource r : top) {
            if (StringUtils.hasText(r.getTopicLabel())) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("keyword", r.getTopicLabel());
                item.put("rank", result.size() + 1);
                result.add(item);
                if (result.size() >= size) break;
            }
        }
        return result.stream().limit(size).toList();
    }

    private String resolveSchoolYear() {
        LocalDate now = LocalDate.now();
        int year = now.getYear();
        if (now.getMonthValue() >= 9) {
            return year + "-" + (year + 1);
        }
        return (year - 1) + "-" + year;
    }

    private String categoryDisplayName(String cat) {
        return switch (cat) {
            case "holiday_hw" -> "寒暑假作业";
            case "term_open" -> "开学备考";
            case "midterm_final" -> "期中期末";
            case "promotion" -> "升学冲刺";
            case "news" -> "时事热点";
            case "cross" -> "跨学科";
            case "project" -> "项目式学习";
            default -> cat;
        };
    }

    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("categories", List.of(
                Map.of("key", "all", "name", "全部", "icon", "📋"),
                Map.of("key", "holiday_hw", "name", "寒暑假作业", "icon", "🏖️"),
                Map.of("key", "term_open", "name", "开学备考", "icon", "🎒"),
                Map.of("key", "midterm_final", "name", "期中期末", "icon", "📝"),
                Map.of("key", "promotion", "name", "升学冲刺", "icon", "🎯"),
                Map.of("key", "news", "name", "时事热点", "icon", "📰"),
                Map.of("key", "cross", "name", "跨学科", "icon", "🔗"),
                Map.of("key", "project", "name", "项目式学习", "icon", "📋")
        ));
        options.put("regions", List.of(
                Map.of("key", "all", "name", "不限"),
                Map.of("key", "chengdu", "name", "成都"),
                Map.of("key", "mianyang", "name", "绵阳"),
                Map.of("key", "sichuan", "name", "四川其他")
        ));
        options.put("gradeStages", List.of(
                Map.of("key", "all", "name", "不限学段"),
                Map.of("key", "primary", "name", "小学"),
                Map.of("key", "junior", "name", "初中"),
                Map.of("key", "senior", "name", "高中")
        ));
        options.put("resourceForms", List.of(
                Map.of("key", "all", "name", "不限形态"),
                Map.of("key", "exam", "name", "试卷/真题"),
                Map.of("key", "material", "name", "讲义/作业"),
                Map.of("key", "lesson_plan", "name", "教案"),
                Map.of("key", "ppt", "name", "课件PPT"),
                Map.of("key", "video", "name", "视频"),
                Map.of("key", "doc", "name", "PDF文档"),
                Map.of("key", "exercise", "name", "练习题")
        ));
        return options;
    }

    private void validateUpload(TopicResourceUploadDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(400, "请填写资源标题");
        }
        if (dto.getTitle().trim().length() > 200) {
            throw new BusinessException(400, "标题不能超过200字");
        }
        if (!StringUtils.hasText(dto.getCategory())) {
            throw new BusinessException(400, "请选择专题场景");
        }
        if (!StringUtils.hasText(dto.getResourceForm())) {
            throw new BusinessException(400, "请选择资源形态");
        }
    }

    private String resolveIcon(TopicResourceUploadDTO dto) {
        if (StringUtils.hasText(dto.getIcon())) {
            return dto.getIcon().trim();
        }
        return CATEGORY_ICONS.getOrDefault(dto.getCategory(), "📚");
    }

    private LambdaQueryWrapper<TopicResource> buildResourceWrapper(TopicQueryDTO dto) {
        LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
        w.eq(TopicResource::getStatus, 1);
        if (StringUtils.hasText(dto.getCategory()) && !"all".equals(dto.getCategory()) && !"elite".equals(dto.getCategory())) {
            w.eq(TopicResource::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getRegion()) && !"all".equals(dto.getRegion())) {
            w.and(q -> q.eq(TopicResource::getRegion, dto.getRegion())
                    .or().eq(TopicResource::getRegion, "all"));
        }
        if (StringUtils.hasText(dto.getGradeStage()) && !"all".equals(dto.getGradeStage())) {
            w.and(q -> q.eq(TopicResource::getGradeStage, dto.getGradeStage())
                    .or().eq(TopicResource::getGradeStage, "all"));
        }
        if (StringUtils.hasText(dto.getSubject()) && !"all".equals(dto.getSubject())) {
            w.eq(TopicResource::getSubject, dto.getSubject());
        }
        if (StringUtils.hasText(dto.getResourceForm()) && !"all".equals(dto.getResourceForm())) {
            w.eq(TopicResource::getResourceForm, mapResourceForm(dto.getResourceForm()));
        }
        if (dto.getIsFree() != null) {
            w.eq(TopicResource::getIsFree, dto.getIsFree());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(TopicResource::getTitle, kw)
                    .or().like(TopicResource::getSummary, kw)
                    .or().like(TopicResource::getTags, kw)
                    .or().like(TopicResource::getTopicLabel, kw));
        }
        return w;
    }

    private LambdaQueryWrapper<TopicAlbum> buildAlbumWrapper(TopicQueryDTO dto) {
        LambdaQueryWrapper<TopicAlbum> w = new LambdaQueryWrapper<>();
        w.eq(TopicAlbum::getStatus, 1);
        if (StringUtils.hasText(dto.getCategory()) && !"all".equals(dto.getCategory())) {
            w.eq(TopicAlbum::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getRegion()) && !"all".equals(dto.getRegion())) {
            w.and(q -> q.eq(TopicAlbum::getRegion, dto.getRegion())
                    .or().eq(TopicAlbum::getRegion, "all"));
        }
        if (StringUtils.hasText(dto.getGradeStage()) && !"all".equals(dto.getGradeStage())) {
            w.and(q -> q.eq(TopicAlbum::getGradeStage, dto.getGradeStage())
                    .or().eq(TopicAlbum::getGradeStage, "all"));
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(TopicAlbum::getTitle, kw)
                    .or().like(TopicAlbum::getSummary, kw)
                    .or().like(TopicAlbum::getTags, kw));
        }
        return w;
    }

    /** 前端筛选 key → 库表 resource_form */
    private String mapResourceForm(String form) {
        return switch (form) {
            case "ppt", "courseware" -> "ppt";
            case "word" -> "lesson_plan";
            case "pdf" -> "doc";
            case "video" -> "video";
            case "exam" -> "exam";
            case "material" -> "material";
            default -> form;
        };
    }

    private void applyResourceSort(LambdaQueryWrapper<TopicResource> w, TopicQueryDTO dto) {
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        String field = dto.getSortField();
        if ("downloadCount".equals(field)) {
            w.orderBy(true, asc, TopicResource::getDownloadCount);
        } else if ("viewCount".equals(field)) {
            w.orderBy(true, asc, TopicResource::getViewCount);
        } else if ("createTime".equals(field)) {
            w.orderBy(true, asc, TopicResource::getCreateTime);
        } else {
            w.orderBy(true, asc, TopicResource::getSort).orderByDesc(TopicResource::getDownloadCount);
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
