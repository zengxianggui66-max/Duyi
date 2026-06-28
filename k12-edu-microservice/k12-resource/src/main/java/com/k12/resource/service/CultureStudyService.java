package com.k12.resource.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.dto.CultureQueryDTO;
import com.k12.common.entity.CulturePackageItem;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.CultureStudyPackage;
import com.k12.common.dto.CultureResourceUploadDTO;
import com.k12.resource.mapper.CulturePackageItemMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.CultureStudyPackageMapper;
import com.k12.resource.search.SearchIndexSyncHook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CultureStudyService {

    private static final Map<String, String> DURATION_LABELS = Map.of(
            "half_day", "半日",
            "one_day", "1日",
            "two_three_day", "2-3日",
            "camp", "营地营期",
            "flexible", "灵活/在线"
    );

    private static final Map<String, String> CATEGORY_ICONS = Map.of(
            "guoxue", "📜",
            "shici", "🖌️",
            "calligraphy", "✒️",
            "festival", "🏮",
            "story", "📖",
            "customs", "🎭",
            "bashu", "🐼",
            "yanxue", "🎒"
    );

    private final CultureResourceMapper cultureResourceMapper;
    private final CultureStudyPackageMapper cultureStudyPackageMapper;
    private final CulturePackageItemMapper culturePackageItemMapper;
    private final FileService fileService;
    private final SearchIndexSyncHook searchIndexSyncHook;
    public CultureStudyService(CultureResourceMapper cultureResourceMapper, CultureStudyPackageMapper cultureStudyPackageMapper, CulturePackageItemMapper culturePackageItemMapper, FileService fileService, SearchIndexSyncHook searchIndexSyncHook) {
        this.cultureResourceMapper = cultureResourceMapper;
        this.cultureStudyPackageMapper = cultureStudyPackageMapper;
        this.culturePackageItemMapper = culturePackageItemMapper;
        this.fileService = fileService;
        this.searchIndexSyncHook = searchIndexSyncHook;
    }


    public Map<String, Object> listResourcesByPage(CultureQueryDTO dto) {
        Page<CultureResource> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<CultureResource> w = buildResourceWrapper(dto);
        applyResourceSort(w, dto);
        IPage<CultureResource> result = cultureResourceMapper.selectPage(page, w);
        return pageResult(result);
    }

    public CultureResource getResource(Long id) {
        CultureResource r = cultureResourceMapper.selectById(id);
        if (r == null) {
            throw new BusinessException(404, "资源不存在");
        }
        return r;
    }

    public void incrementResourceView(Long id) {
        CultureResource r = getResource(id);
        r.setViewCount((r.getViewCount() != null ? r.getViewCount() : 0) + 1);
        cultureResourceMapper.updateById(r);
    }

    public void incrementResourceDownload(Long id) {
        CultureResource r = getResource(id);
        if (!"platform".equals(r.getResourceKind())) {
            throw new BusinessException(400, "外链资源请前往来源网站查看");
        }
        r.setDownloadCount((r.getDownloadCount() != null ? r.getDownloadCount() : 0) + 1);
        cultureResourceMapper.updateById(r);
    }

    public Map<String, Object> listPackagesByPage(CultureQueryDTO dto) {
        Page<CultureStudyPackage> page = new Page<>(dto.getCurrent(), dto.getSize());
        LambdaQueryWrapper<CultureStudyPackage> w = buildPackageWrapper(dto);
        applyPackageSort(w, dto);
        IPage<CultureStudyPackage> result = cultureStudyPackageMapper.selectPage(page, w);
        return pageResult(result);
    }

    @SuppressWarnings("deprecation")
    public Map<String, Object> getPackageDetail(Long id) {
        CultureStudyPackage pkg = cultureStudyPackageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "研学包不存在");
        }
        List<CulturePackageItem> items = culturePackageItemMapper.selectList(
                new LambdaQueryWrapper<CulturePackageItem>()
                        .eq(CulturePackageItem::getPackageId, id)
                        .orderByAsc(CulturePackageItem::getSort));
        List<Long> resIds = items.stream().map(CulturePackageItem::getResourceId).toList();
        List<CultureResource> resources = resIds.isEmpty() ? List.of()
                : cultureResourceMapper.selectBatchIds(resIds);
        Map<Long, CultureResource> resMap = resources.stream()
                .collect(Collectors.toMap(CultureResource::getId, r -> r, (a, b) -> a));
        List<CultureResource> ordered = resIds.stream()
                .map(resMap::get)
                .filter(Objects::nonNull)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("package", pkg);
        data.put("resources", ordered);
        return data;
    }

    public void incrementPackageDownload(Long id) {
        CultureStudyPackage pkg = cultureStudyPackageMapper.selectById(id);
        if (pkg == null) {
            throw new BusinessException(404, "研学包不存在");
        }
        pkg.setDownloadCount((pkg.getDownloadCount() != null ? pkg.getDownloadCount() : 0) + 1);
        cultureStudyPackageMapper.updateById(pkg);
    }

    @Transactional
    public CultureResource createResource(MultipartFile file, CultureResourceUploadDTO dto) {
        validateUpload(dto);

        String kind = StringUtils.hasText(dto.getResourceKind()) ? dto.getResourceKind() : "platform";
        CultureResource resource = new CultureResource();
        resource.setTitle(dto.getTitle().trim());
        resource.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        resource.setCategory(dto.getCategory());
        resource.setRegion(dto.getRegion());
        resource.setDurationType(dto.getDurationType());
        resource.setDurationLabel(resolveDurationLabel(dto));
        resource.setSuitableAudience(
                StringUtils.hasText(dto.getSuitableAudience()) ? dto.getSuitableAudience().trim() : "研学机构");
        resource.setLocation(StringUtils.hasText(dto.getLocation()) ? dto.getLocation().trim() : "");
        resource.setResourceKind(kind);
        resource.setTags(StringUtils.hasText(dto.getTags()) ? dto.getTags().trim() : "");
        resource.setIcon(resolveIcon(dto));
        resource.setIsFree(dto.getIsFree() != null ? dto.getIsFree() : 1);
        resource.setIsElite(0);
        resource.setStatus(1);
        resource.setSort(0);
        resource.setDownloadCount(0);
        resource.setViewCount(0);

        if ("external".equals(kind)) {
            resource.setExternalUrl(dto.getExternalUrl().trim());
            resource.setSourceName(StringUtils.hasText(dto.getSourceName()) ? dto.getSourceName().trim() : "用户推荐");
            resource.setFileFormat("链接");
            resource.setFileUrl("");
        } else {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(400, "请上传资源文件");
            }
            try {
                Map<String, Object> uploadResult = fileService.uploadFile(file);
                resource.setFileUrl((String) uploadResult.get("fileUrl"));
                String ext = (String) uploadResult.get("fileFormat");
                resource.setFileFormat(ext != null ? ext.toUpperCase() : "FILE");
            } catch (Exception e) {
                throw new BusinessException(500, "文件上传失败: " + e.getMessage());
            }
        }

        cultureResourceMapper.insert(resource);
        searchIndexSyncHook.afterCultureChanged(resource.getId());
        return resource;
    }

    private void validateUpload(CultureResourceUploadDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException(400, "请填写资源标题");
        }
        if (dto.getTitle().trim().length() > 200) {
            throw new BusinessException(400, "标题不能超过200字");
        }
        if (!StringUtils.hasText(dto.getCategory())) {
            throw new BusinessException(400, "请选择主题分类");
        }
        if (!StringUtils.hasText(dto.getRegion())) {
            throw new BusinessException(400, "请选择地域");
        }
        if (!StringUtils.hasText(dto.getDurationType())) {
            throw new BusinessException(400, "请选择研学时长");
        }
        String kind = StringUtils.hasText(dto.getResourceKind()) ? dto.getResourceKind() : "platform";
        if ("external".equals(kind)) {
            if (!StringUtils.hasText(dto.getExternalUrl())) {
                throw new BusinessException(400, "请填写外链地址");
            }
            if (!dto.getExternalUrl().trim().matches("^https?://.+")) {
                throw new BusinessException(400, "外链地址需以 http:// 或 https:// 开头");
            }
        }
    }

    private String resolveDurationLabel(CultureResourceUploadDTO dto) {
        if (StringUtils.hasText(dto.getDurationLabel())) {
            return dto.getDurationLabel().trim();
        }
        return DURATION_LABELS.getOrDefault(dto.getDurationType(), dto.getDurationType());
    }

    private String resolveIcon(CultureResourceUploadDTO dto) {
        if (StringUtils.hasText(dto.getIcon())) {
            return dto.getIcon().trim();
        }
        return CATEGORY_ICONS.getOrDefault(dto.getCategory(), "📜");
    }

    public Map<String, Object> getFilterOptions() {
        Map<String, Object> options = new LinkedHashMap<>();
        options.put("categories", List.of(
                Map.of("key", "all", "name", "全部", "icon", "📋"),
                Map.of("key", "guoxue", "name", "国学经典", "icon", "📜"),
                Map.of("key", "shici", "name", "诗歌鉴赏", "icon", "🖌️"),
                Map.of("key", "calligraphy", "name", "书法美术", "icon", "✒️"),
                Map.of("key", "festival", "name", "传统节日", "icon", "🏮"),
                Map.of("key", "story", "name", "民间故事", "icon", "📖"),
                Map.of("key", "customs", "name", "民俗文化", "icon", "🎭"),
                Map.of("key", "bashu", "name", "巴蜀·成都", "icon", "🐼"),
                Map.of("key", "yanxue", "name", "研学方案", "icon", "🎒")
        ));
        options.put("regions", List.of(
                Map.of("key", "all", "name", "全部地域"),
                Map.of("key", "chengdu", "name", "成都"),
                Map.of("key", "bashu", "name", "巴蜀"),
                Map.of("key", "sichuan", "name", "四川")
        ));
        options.put("durations", List.of(
                Map.of("key", "all", "name", "不限时长"),
                Map.of("key", "half_day", "name", "半日"),
                Map.of("key", "one_day", "name", "1日"),
                Map.of("key", "two_three_day", "name", "2-3日"),
                Map.of("key", "camp", "name", "营地营期"),
                Map.of("key", "flexible", "name", "灵活/在线")
        ));
        options.put("resourceKinds", List.of(
                Map.of("key", "all", "name", "全部类型"),
                Map.of("key", "platform", "name", "平台资源"),
                Map.of("key", "external", "name", "延展阅读")
        ));
        return options;
    }

    private LambdaQueryWrapper<CultureResource> buildResourceWrapper(CultureQueryDTO dto) {
        LambdaQueryWrapper<CultureResource> w = new LambdaQueryWrapper<>();
        w.eq(CultureResource::getStatus, 1);
        w.and(q -> q.isNull(CultureResource::getDeleted).or().eq(CultureResource::getDeleted, 0));
        if (StringUtils.hasText(dto.getCategory()) && !"all".equals(dto.getCategory())) {
            w.eq(CultureResource::getCategory, dto.getCategory());
        }
        if (StringUtils.hasText(dto.getRegion()) && !"all".equals(dto.getRegion())) {
            applyRegionFilter(w, dto.getRegion());
        }
        if (StringUtils.hasText(dto.getDurationType()) && !"all".equals(dto.getDurationType())) {
            w.eq(CultureResource::getDurationType, dto.getDurationType());
        }
        if (StringUtils.hasText(dto.getResourceKind()) && !"all".equals(dto.getResourceKind())) {
            w.eq(CultureResource::getResourceKind, dto.getResourceKind());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(CultureResource::getTitle, kw)
                    .or().like(CultureResource::getSummary, kw)
                    .or().like(CultureResource::getTags, kw)
                    .or().like(CultureResource::getLocation, kw));
        }
        return w;
    }

    private LambdaQueryWrapper<CultureStudyPackage> buildPackageWrapper(CultureQueryDTO dto) {
        LambdaQueryWrapper<CultureStudyPackage> w = new LambdaQueryWrapper<>();
        w.eq(CultureStudyPackage::getStatus, 1);
        if (StringUtils.hasText(dto.getRegion()) && !"all".equals(dto.getRegion())) {
            applyPackageRegionFilter(w, dto.getRegion());
        }
        if (StringUtils.hasText(dto.getDurationType()) && !"all".equals(dto.getDurationType())) {
            w.eq(CultureStudyPackage::getDurationType, dto.getDurationType());
        }
        if (StringUtils.hasText(dto.getKeyword())) {
            String kw = dto.getKeyword().trim();
            w.and(q -> q.like(CultureStudyPackage::getTitle, kw)
                    .or().like(CultureStudyPackage::getSummary, kw)
                    .or().like(CultureStudyPackage::getLocation, kw));
        }
        return w;
    }

    private void applyResourceSort(LambdaQueryWrapper<CultureResource> w, CultureQueryDTO dto) {
        boolean asc = "asc".equalsIgnoreCase(dto.getSortOrder());
        String field = dto.getSortField();
        if ("downloadCount".equals(field)) {
            w.orderBy(true, asc, CultureResource::getDownloadCount);
        } else if ("viewCount".equals(field)) {
            w.orderBy(true, asc, CultureResource::getViewCount);
        } else if ("createTime".equals(field)) {
            w.orderBy(true, asc, CultureResource::getCreateTime);
        } else {
            w.orderByDesc(CultureResource::getIsElite).orderByDesc(CultureResource::getSort);
        }
    }

    private void applyPackageSort(LambdaQueryWrapper<CultureStudyPackage> w, CultureQueryDTO dto) {
        w.orderByDesc(CultureStudyPackage::getIsElite)
                .orderByDesc(CultureStudyPackage::getSort)
                .orderByDesc(CultureStudyPackage::getDownloadCount);
    }

    private void applyRegionFilter(LambdaQueryWrapper<CultureResource> w, String region) {
        switch (region) {
            case "bashu" -> w.in(CultureResource::getRegion, "bashu", "chengdu", "sichuan");
            case "sichuan" -> w.in(CultureResource::getRegion, "sichuan", "bashu", "chengdu");
            case "chengdu" -> w.in(CultureResource::getRegion, "chengdu", "bashu");
            default -> w.eq(CultureResource::getRegion, region);
        }
    }

    private void applyPackageRegionFilter(LambdaQueryWrapper<CultureStudyPackage> w, String region) {
        switch (region) {
            case "bashu" -> w.in(CultureStudyPackage::getRegion, "bashu", "chengdu", "sichuan");
            case "sichuan" -> w.in(CultureStudyPackage::getRegion, "sichuan", "bashu", "chengdu");
            case "chengdu" -> w.in(CultureStudyPackage::getRegion, "chengdu", "bashu");
            default -> w.eq(CultureStudyPackage::getRegion, region);
        }
    }

    private <T> Map<String, Object> pageResult(IPage<T> result) {
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("current", result.getCurrent());
        map.put("size", result.getSize());
        map.put("pages", result.getPages());
        return map;
    }
}
