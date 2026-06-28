package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.constant.ResourceStatusConstants;
import com.k12.common.constant.ResourceTypeConstants;
import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.ResourceMainDetailVO;
import com.k12.common.dto.ResourceMainQueryDTO;
import com.k12.common.dto.ResourceMainVO;
import com.k12.common.entity.EduResourceFile;
import com.k12.common.entity.PreviewFailQueue;
import com.k12.common.entity.VAdminResourceMain;
import com.k12.resource.adapter.ResourceSourceAdapter;
import com.k12.resource.adapter.ResourceSourceAdapterRegistry;
import com.k12.resource.mapper.EduResourceFileMapper;
import com.k12.resource.mapper.PreviewFailQueueMapper;
import com.k12.resource.mapper.ResourceMainMapper;
import com.k12.resource.mapper.VAdminResourceMainMapper;
import com.k12.resource.service.CollectionService;
import com.k12.resource.service.DocumentPreviewService;
import com.k12.resource.service.ResourceMainService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@SuppressWarnings("null")
public class ResourceMainServiceImpl implements ResourceMainService {

    private static final Set<String> SORT_FIELDS = Set.of(
            "upload_time", "download_count", "view_count", "sort");

    private final VAdminResourceMainMapper viewMapper;
    private final ResourceMainMapper resourceMainMapper;
    private final EduResourceFileMapper eduResourceFileMapper;
    private final DocumentPreviewService documentPreviewService;
    private final PreviewFailQueueMapper previewFailQueueMapper;
    private final ResourceSourceAdapterRegistry adapterRegistry;
    private final CollectionService collectionService;

    public ResourceMainServiceImpl(VAdminResourceMainMapper viewMapper,
                                   ResourceMainMapper resourceMainMapper,
                                   EduResourceFileMapper eduResourceFileMapper,
                                   DocumentPreviewService documentPreviewService,
                                   PreviewFailQueueMapper previewFailQueueMapper,
                                   ResourceSourceAdapterRegistry adapterRegistry,
                                   CollectionService collectionService) {
        this.viewMapper = viewMapper;
        this.resourceMainMapper = resourceMainMapper;
        this.eduResourceFileMapper = eduResourceFileMapper;
        this.documentPreviewService = documentPreviewService;
        this.previewFailQueueMapper = previewFailQueueMapper;
        this.adapterRegistry = adapterRegistry;
        this.collectionService = collectionService;
    }

    @Override
    public Page<ResourceMainVO> page(ResourceMainQueryDTO query) {
        ResourceMainQueryDTO q = normalizePublicQuery(query);
        Page<VAdminResourceMain> page = viewMapper.findPage(new Page<>(q.getCurrent(), q.getSize()), q);
        Page<ResourceMainVO> out = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        List<ResourceMainVO> records = page.getRecords().stream().map(ResourceMainVO::fromEntity).toList();
        out.setRecords(records);
        return out;
    }

    @Override
    public Long resolveGlobalId(String sourceType, Long sourceId) {
        if (!StringUtils.hasText(sourceType) || sourceId == null || sourceId <= 0) {
            throw new BusinessException(400, "sourceType/sourceId 参数无效");
        }
        Long globalId = resourceMainMapper.findGlobalId(sourceType, sourceId);
        if (globalId == null) {
            throw new BusinessException(404, "未找到对应统一资源");
        }
        return globalId;
    }

    @Override
    public ResourceMainDetailVO detail(Long resourceId) {
        VAdminResourceMain row = requirePublicResource(resourceId);
        return ResourceMainDetailVO.fromEntity(row);
    }

    @Override
    public Map<String, Object> stats(ResourceMainQueryDTO query) {
        ResourceMainQueryDTO q = normalizePublicQuery(query);
        Page<VAdminResourceMain> page = viewMapper.findPage(new Page<>(1, 1), q);
        long total = page.getTotal();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", total);
        result.put("current", q.getCurrent());
        result.put("size", q.getSize());
        return result;
    }

    @Override
    public List<Map<String, Object>> types(ResourceMainQueryDTO query) {
        ResourceMainQueryDTO q = normalizePublicQuery(query);
        q.setCurrent(1);
        q.setSize(1000);
        Page<VAdminResourceMain> page = viewMapper.findPage(new Page<>(1, 1000), q);
        Map<String, Long> counter = new LinkedHashMap<>();
        for (VAdminResourceMain row : page.getRecords()) {
            String type = StringUtils.hasText(row.getType()) ? row.getType() : "其他";
            counter.put(type, counter.getOrDefault(type, 0L) + 1);
        }
        List<Map<String, Object>> types = new ArrayList<>();
        counter.forEach((k, v) -> {
            Map<String, Object> m = new HashMap<>();
            m.put("type", k);
            m.put("count", v);
            types.add(m);
        });
        return types;
    }

    @Override
    public FilePreviewInfoDTO preview(Long resourceId) {
        VAdminResourceMain row = requirePublicResource(resourceId);
        String candidateUrl = resolvePlatformOrFallbackUrl(row);
        if (!StringUtils.hasText(candidateUrl)) {
            enqueuePreviewFail(row, "文件地址为空");
            return FilePreviewInfoDTO.builder()
                    .previewMode("none")
                    .previewType("document")
                    .provider("none")
                    .message("资源无可预览文件")
                    .build();
        }
        // 外链资源：直接跳转源地址，不走平台转码
        if (isExternalResource(row, candidateUrl)) {
            return FilePreviewInfoDTO.builder()
                    .previewUrl(candidateUrl)
                    .originalUrl(candidateUrl)
                    .previewMode("external")
                    .previewType("document")
                    .provider("external")
                    .converted(false)
                    .message("外链资源，请前往来源站点查看")
                    .build();
        }
        FilePreviewInfoDTO info = documentPreviewService.resolvePreview(candidateUrl);
        if (info == null || "none".equalsIgnoreCase(info.getPreviewMode())) {
            enqueuePreviewFail(row, info != null ? info.getMessage() : "预览服务未返回结果");
        }
        return info;
    }

    @Override
    public Map<String, Object> download(Long resourceId, Long userId) {
        VAdminResourceMain row = requirePublicResource(resourceId);
        String candidateUrl = resolvePlatformOrFallbackUrl(row);
        if (!StringUtils.hasText(candidateUrl)) {
            throw new BusinessException(404, "资源文件不存在");
        }
        if (!isExternalResource(row, candidateUrl)) {
            incrementDownloadBySource(row);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("resourceId", resourceId);
        result.put("sourceType", row.getSourceType());
        result.put("sourceId", row.getSourceId());
        result.put("title", row.getTitle());
        result.put("downloadUrl", candidateUrl);
        result.put("external", isExternalResource(row, candidateUrl));
        result.put("userId", userId);
        return result;
    }

    @Override
    public void view(Long resourceId, Long userId) {
        VAdminResourceMain row = requirePublicResource(resourceId);
        incrementViewBySource(row);
    }

    @Override
    public void collect(Long resourceId, Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录");
        }
        VAdminResourceMain row = requirePublicResource(resourceId);
        ResourceSourceAdapter adapter = adapterRegistry.get(row.getSourceType());
        String resourceType = adapter != null
                ? adapter.collectResourceType()
                : ResourceTypeConstants.RESOURCE;
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("title", row.getTitle());
        snapshot.put("stage", row.getStage());
        snapshot.put("subject", row.getSubject());
        snapshot.put("module", row.getModule());
        snapshot.put("type", row.getType());
        snapshot.put("gradeName", row.getGradeName());
        snapshot.put("fileExt", row.getFileExt());
        snapshot.put("ossUrl", row.getOssUrl());
        collectionService.collectWithSnapshot(userId, row.getSourceId(), resourceType, snapshot);
    }

    private ResourceMainQueryDTO normalizePublicQuery(ResourceMainQueryDTO query) {
        ResourceMainQueryDTO q = query != null ? query : new ResourceMainQueryDTO();
        q.setAuditStatus(ResourceStatusConstants.AUDIT_APPROVED);
        q.setPublishStatus(ResourceStatusConstants.PUBLISH_PUBLISHED);
        q.setStatus(ResourceStatusConstants.PUBLISHED);
        if (!StringUtils.hasText(q.getSortField())) {
            q.setSortField("upload_time");
        }
        String normalized = q.getSortField().replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
        if (!SORT_FIELDS.contains(normalized)) {
            normalized = "upload_time";
        }
        q.setSortField(normalized);
        q.setSortOrder("asc".equalsIgnoreCase(q.getSortOrder()) ? "asc" : "desc");
        if (q.getCurrent() == null || q.getCurrent() < 1) {
            q.setCurrent(1);
        }
        if (q.getSize() == null || q.getSize() < 1) {
            q.setSize(20);
        } else if (q.getSize() > 100) {
            q.setSize(100);
        }
        return q;
    }

    private VAdminResourceMain requirePublicResource(Long resourceId) {
        if (resourceId == null || resourceId <= 0) {
            throw new BusinessException(400, "resourceId 无效");
        }
        VAdminResourceMain row = viewMapper.findByGlobalId(resourceId);
        if (row == null) {
            throw new BusinessException(404, "资源不存在");
        }
        if (!Integer.valueOf(ResourceStatusConstants.AUDIT_APPROVED).equals(row.getAuditStatus())
                || !Integer.valueOf(ResourceStatusConstants.PUBLISH_PUBLISHED).equals(row.getPublishStatus())) {
            throw new BusinessException(403, "资源未上架或不可访问");
        }
        return row;
    }

    private String resolvePlatformOrFallbackUrl(VAdminResourceMain row) {
        if (row == null || row.getSourceId() == null) {
            return null;
        }
        EduResourceFile mainFile = eduResourceFileMapper.selectOne(
                new LambdaQueryWrapper<EduResourceFile>()
                        .eq(EduResourceFile::getResourceId, row.getSourceId())
                        .eq(EduResourceFile::getStatus, 1)
                        .orderByAsc(EduResourceFile::getSort)
                        .last("LIMIT 1")
        );
        if (mainFile != null && StringUtils.hasText(mainFile.getOssUrl())) {
            return mainFile.getOssUrl();
        }
        return row.getOssUrl();
    }

    private boolean isExternalResource(VAdminResourceMain row, String url) {
        if (!StringUtils.hasText(url) || row == null) {
            return false;
        }
        ResourceSourceAdapter adapter = adapterRegistry.get(row.getSourceType());
        return adapter != null && adapter.isExternalPreview(row.getAllowPreview(), url);
    }

    private void incrementViewBySource(VAdminResourceMain row) {
        ResourceSourceAdapter adapter = adapterRegistry.get(row.getSourceType());
        if (adapter != null) {
            adapter.incrementView(row.getSourceId());
        }
    }

    private void incrementDownloadBySource(VAdminResourceMain row) {
        ResourceSourceAdapter adapter = adapterRegistry.get(row.getSourceType());
        if (adapter != null) {
            adapter.incrementDownload(row.getSourceId());
        }
    }

    private void enqueuePreviewFail(VAdminResourceMain row, String reason) {
        if (row == null) {
            return;
        }
        PreviewFailQueue existing = previewFailQueueMapper.selectOne(
                new LambdaQueryWrapper<PreviewFailQueue>()
                        .eq(PreviewFailQueue::getGlobalId, row.getGlobalId())
                        .eq(PreviewFailQueue::getStatus, 0)
                        .last("LIMIT 1")
        );
        if (existing != null) {
            existing.setFailCount((existing.getFailCount() == null ? 0 : existing.getFailCount()) + 1);
            existing.setLastFailTime(LocalDateTime.now());
            existing.setFailReason(reason);
            previewFailQueueMapper.updateById(existing);
            return;
        }
        PreviewFailQueue fail = new PreviewFailQueue();
        fail.setSourceType(row.getSourceType());
        fail.setSourceId(row.getSourceId());
        fail.setGlobalId(row.getGlobalId());
        fail.setTitle(row.getTitle());
        fail.setFailReason(StringUtils.hasText(reason) ? reason : "预览失败");
        fail.setFailCount(1);
        fail.setLastFailTime(LocalDateTime.now());
        fail.setStatus(0);
        previewFailQueueMapper.insert(fail);
    }
}
