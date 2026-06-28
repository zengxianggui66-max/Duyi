package com.k12.resource.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.TopicResource;
import com.k12.resource.entity.ResourceSearchIndex;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.ResourceSearchIndexMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.util.PublicResourceQuerySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 搜索索引构建：从各资源表同步到 resource_search_index（P0+ 离线/增量）
 */
@Slf4j
@Service
public class SearchIndexSyncService {

    private static final Map<String, String> STAGE_CN_TO_KEY = Map.ofEntries(
            Map.entry("幼儿", "preschool"),
            Map.entry("学前", "preschool"),
            Map.entry("小学", "primary"),
            Map.entry("初中", "junior"),
            Map.entry("高中", "senior"),
            Map.entry("美术", "art"),
            Map.entry("舞蹈", "dance"),
            Map.entry("preschool", "preschool"),
            Map.entry("primary", "primary"),
            Map.entry("junior", "junior"),
            Map.entry("senior", "senior"),
            Map.entry("art", "art"),
            Map.entry("dance", "dance")
    );

    private static final Map<String, String> STAGE_KEY_TO_NAME = Map.of(
            "preschool", "幼儿",
            "primary", "小学",
            "junior", "初中",
            "senior", "高中",
            "art", "美术",
            "dance", "舞蹈"
    );

    private final ResourceSearchIndexMapper indexMapper;
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final TopicResourceMapper topicResourceMapper;
    private final CompetitionResourceMapper competitionResourceMapper;
    private final CultureResourceMapper cultureResourceMapper;
    public SearchIndexSyncService(ResourceSearchIndexMapper indexMapper, PrimaryChineseResourceMapper primaryChineseResourceMapper, TopicResourceMapper topicResourceMapper, CompetitionResourceMapper competitionResourceMapper, CultureResourceMapper cultureResourceMapper) {
        this.indexMapper = indexMapper;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.topicResourceMapper = topicResourceMapper;
        this.competitionResourceMapper = competitionResourceMapper;
        this.cultureResourceMapper = cultureResourceMapper;
    }


    public long countIndexRows() {
        return indexMapper.selectCount(null);
    }

    @Async
    public void rebuildAllAsync() {
        rebuildAll();
    }

    public int rebuildAll() {
        log.info("开始全量重建 resource_search_index");
        indexMapper.delete(new LambdaQueryWrapper<>());
        int total = 0;
        total += syncPrimaryChineseAll();
        total += syncTopicAll();
        total += syncCompetitionAll();
        total += syncCultureAll();
        log.info("resource_search_index 全量重建完成，共 {} 条", total);
        return total;
    }

    public void upsertPrimaryChinese(PrimaryChineseResource r) {
        if (r == null || r.getId() == null) return;
        if (!PublicResourceQuerySupport.isPubliclyPublished(r)) {
            deleteDoc("primary_chinese", r.getId());
            return;
        }
        upsert(toPrimaryIndex(r));
    }

    public void upsertTopic(TopicResource r) {
        if (r == null || r.getId() == null) return;
        if (!Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("topic", r.getId());
            return;
        }
        upsert(toTopicIndex(r));
    }

    public void upsertCompetition(CompetitionResource r) {
        if (r == null || r.getId() == null) return;
        if (!Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("competition", r.getId());
            return;
        }
        upsert(toCompetitionIndex(r));
    }

    public void upsertCulture(CultureResource r) {
        if (r == null || r.getId() == null) return;
        if (!Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("culture", r.getId());
            return;
        }
        upsert(toCultureIndex(r));
    }

    @Async
    public void syncPrimaryChineseByIdAsync(Long id) {
        syncPrimaryChineseById(id);
    }

    @Async
    public void syncTopicByIdAsync(Long id) {
        syncTopicById(id);
    }

    @Async
    public void syncCompetitionByIdAsync(Long id) {
        syncCompetitionById(id);
    }

    @Async
    public void syncCultureByIdAsync(Long id) {
        syncCultureById(id);
    }

    public void syncPrimaryChineseById(Long id) {
        if (id == null) return;
        PrimaryChineseResource r = primaryChineseResourceMapper.selectById(id);
        if (r == null) {
            deleteDoc("primary_chinese", id);
            return;
        }
        upsertPrimaryChinese(r);
    }

    public void syncTopicById(Long id) {
        if (id == null) return;
        TopicResource r = topicResourceMapper.selectById(id);
        if (r == null) {
            deleteDoc("topic", id);
            return;
        }
        upsertTopic(r);
    }

    public void syncCompetitionById(Long id) {
        if (id == null) return;
        CompetitionResource r = competitionResourceMapper.selectById(id);
        if (r == null) {
            deleteDoc("competition", id);
            return;
        }
        upsertCompetition(r);
    }

    public void syncCultureById(Long id) {
        if (id == null) return;
        CultureResource r = cultureResourceMapper.selectById(id);
        if (r == null) {
            deleteDoc("culture", id);
            return;
        }
        upsertCulture(r);
    }

    private int syncPrimaryChineseAll() {
        LambdaQueryWrapper<PrimaryChineseResource> w = new LambdaQueryWrapper<>();
        w.eq(PrimaryChineseResource::getIsDeleted, 0).eq(PrimaryChineseResource::getStatus, 1);
        List<PrimaryChineseResource> list = primaryChineseResourceMapper.selectList(w);
        for (PrimaryChineseResource r : list) {
            upsert(toPrimaryIndex(r));
        }
        return list.size();
    }

    private ResourceSearchIndex toPrimaryIndex(PrimaryChineseResource r) {
        String stageKey = resolveStageKey(r.getStage());
        String stageName = STAGE_KEY_TO_NAME.getOrDefault(stageKey, r.getStage());
        String summary = defaultText(r.getRemark(), r.getLessonName());
        String searchText = joinText(
                r.getTitle(), r.getRemark(), r.getLessonName(), r.getUnitName(),
                r.getModule(), r.getEdition(), r.getSubject(), r.getGradeName(),
                r.getType(), r.getCatalogPath(), stageName
        );
        ResourceSearchIndex idx = baseIndex(
                "primary_chinese", r.getId(), r.getTitle(), summary, searchText,
                stageKey, stageName, "stage_resource", "学段资源",
                r.getSubject(), r.getGradeName(), r.getEdition(), r.getModule(),
                r.getCatalogPath(), r.getLessonName(), r.getType(), r.getFileExt(),
                "/resource/" + r.getId() + "?from=subject",
                r.getDownloadCount(), r.getViewCount(), 0,
                r.getUploadTime());
        return idx;
    }

    private int syncTopicAll() {
        LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
        w.eq(TopicResource::getDeleted, 0).eq(TopicResource::getStatus, 1);
        List<TopicResource> list = topicResourceMapper.selectList(w);
        for (TopicResource r : list) {
            upsert(toTopicIndex(r));
        }
        return list.size();
    }

    private ResourceSearchIndex toTopicIndex(TopicResource r) {
        String summary = defaultText(r.getSummary(), r.getTopicLabel());
        String searchText = joinText(
                r.getTitle(), r.getSummary(), r.getTags(), r.getTopicLabel(),
                r.getSubject(), r.getCategory(), r.getRegion(), r.getResourceForm()
        );
        return baseIndex(
                "topic", r.getId(), r.getTitle(), summary, searchText,
                null, null, "topic", "专题资源",
                r.getSubject(), r.getGradeStage(), null, r.getCategory(),
                null, null, r.getResourceForm(), r.getFileFormat(),
                "/topic-zone/resource/" + r.getId(),
                r.getDownloadCount(), r.getViewCount(), vipFlag(r.getIsFree(), r.getIsElite()),
                r.getCreateTime());
    }

    private int syncCompetitionAll() {
        LambdaQueryWrapper<CompetitionResource> w = new LambdaQueryWrapper<>();
        w.eq(CompetitionResource::getDeleted, 0).eq(CompetitionResource::getStatus, 1);
        List<CompetitionResource> list = competitionResourceMapper.selectList(w);
        for (CompetitionResource r : list) {
            upsert(toCompetitionIndex(r));
        }
        return list.size();
    }

    private ResourceSearchIndex toCompetitionIndex(CompetitionResource r) {
        String stageKey = resolveStageKey(r.getGradeStage());
        String stageName = STAGE_KEY_TO_NAME.getOrDefault(stageKey, r.getGradeStage());
        String summary = defaultText(r.getSummary(), r.getCompetitionName());
        String searchText = joinText(
                r.getTitle(), r.getSummary(), r.getTags(), r.getCompetitionName(),
                r.getSubject(), r.getResourceForm(), stageName
        );
        return baseIndex(
                "competition", r.getId(), r.getTitle(), summary, searchText,
                stageKey, stageName, "competition", "竞赛专区",
                r.getSubject(), null, null, r.getCategory(),
                null, null, r.getResourceForm(), r.getFileFormat(),
                "/competition-zone/resource/" + r.getId(),
                r.getDownloadCount(), r.getViewCount(), vipFlag(r.getIsFree(), r.getIsElite()),
                r.getCreateTime());
    }

    private int syncCultureAll() {
        LambdaQueryWrapper<CultureResource> w = new LambdaQueryWrapper<>();
        w.eq(CultureResource::getDeleted, 0).eq(CultureResource::getStatus, 1);
        List<CultureResource> list = cultureResourceMapper.selectList(w);
        for (CultureResource r : list) {
            upsert(toCultureIndex(r));
        }
        return list.size();
    }

    private ResourceSearchIndex toCultureIndex(CultureResource r) {
        String summary = defaultText(r.getSummary(), r.getLocation());
        String searchText = joinText(
                r.getTitle(), r.getSummary(), r.getTags(), r.getLocation(),
                r.getCategory(), r.getResourceKind(), r.getDurationLabel()
        );
        return baseIndex(
                "culture", r.getId(), r.getTitle(), summary, searchText,
                null, null, "culture", "传统文化",
                null, null, null, r.getCategory(),
                null, null, r.getResourceKind(), r.getFileFormat(),
                "/culture",
                r.getDownloadCount(), r.getViewCount(), vipFlag(r.getIsFree(), r.getIsElite()),
                r.getCreateTime());
    }

    private ResourceSearchIndex baseIndex(
            String resourceType,
            Long resourceId,
            String title,
            String summary,
            String searchText,
            String stageKey,
            String stageName,
            String channelKey,
            String channelName,
            String subject,
            String gradeName,
            String editionName,
            String moduleName,
            String catalogPath,
            String lessonName,
            String teachingType,
            String fileExt,
            String detailRoute,
            Integer downloadCount,
            Integer viewCount,
            int vipFlag,
            LocalDateTime publishTime) {
        ResourceSearchIndex idx = new ResourceSearchIndex();
        idx.setDocId(resourceType + ":" + resourceId);
        idx.setResourceId(resourceId);
        idx.setResourceType(resourceType);
        idx.setTitle(title);
        idx.setSummary(summary);
        idx.setSearchText(searchText);
        idx.setStageKey(stageKey);
        idx.setStageName(stageName);
        idx.setChannelKey(channelKey);
        idx.setChannelName(channelName);
        idx.setSubject(subject);
        idx.setGradeName(gradeName);
        idx.setEditionName(editionName);
        idx.setModuleName(moduleName);
        idx.setCatalogPath(catalogPath);
        idx.setLessonName(lessonName);
        idx.setTeachingType(teachingType);
        idx.setFileExt(fileExt);
        idx.setDetailRoute(detailRoute);
        idx.setDownloadCount(nvl(downloadCount));
        idx.setViewCount(nvl(viewCount));
        idx.setHotScore(calcHotScore(downloadCount, viewCount, publishTime));
        idx.setVipFlag(vipFlag);
        idx.setStatus(1);
        idx.setPublishTime(publishTime);
        return idx;
    }

    private void upsert(ResourceSearchIndex doc) {
        ResourceSearchIndex existing = indexMapper.selectOne(
                new LambdaQueryWrapper<ResourceSearchIndex>().eq(ResourceSearchIndex::getDocId, doc.getDocId()));
        if (existing != null) {
            doc.setId(existing.getId());
            indexMapper.updateById(doc);
        } else {
            indexMapper.insert(doc);
        }
    }

    private void deleteDoc(String resourceType, Long resourceId) {
        indexMapper.delete(new LambdaQueryWrapper<ResourceSearchIndex>()
                .eq(ResourceSearchIndex::getDocId, resourceType + ":" + resourceId));
    }

    private String resolveStageKey(String stage) {
        if (!StringUtils.hasText(stage)) return null;
        String trimmed = stage.trim();
        if (STAGE_CN_TO_KEY.containsKey(trimmed)) {
            return STAGE_CN_TO_KEY.get(trimmed);
        }
        String lower = trimmed.toLowerCase(Locale.ROOT);
        return STAGE_CN_TO_KEY.getOrDefault(lower, lower);
    }

    private int vipFlag(Integer isFree, Integer isElite) {
        if (Integer.valueOf(1).equals(isElite)) return 1;
        if (isFree != null && isFree == 0) return 1;
        return 0;
    }

    private double calcHotScore(Integer downloadCount, Integer viewCount, LocalDateTime publishTime) {
        double score = Math.log1p(nvl(downloadCount)) * 0.35 + Math.log1p(nvl(viewCount)) * 0.15;
        if (publishTime != null && publishTime.isAfter(LocalDateTime.now().minusYears(3))) {
            score += 0.5;
        }
        return score;
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private String defaultText(String primary, String fallback) {
        if (StringUtils.hasText(primary)) return primary;
        if (StringUtils.hasText(fallback)) return fallback;
        return "暂无摘要";
    }

    private String joinText(String... parts) {
        return Stream.of(parts)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .reduce((a, b) -> a + " " + b)
                .orElse("");
    }
}
