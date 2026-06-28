package com.k12.resource.search;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.entity.Article;
import com.k12.common.entity.CompetitionResource;
import com.k12.common.entity.CultureResource;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.TopicResource;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.mapper.ArticleSearchMapper;
import com.k12.resource.mapper.CompetitionResourceMapper;
import com.k12.resource.mapper.CultureResourceMapper;
import com.k12.resource.mapper.EduResourceSearchSyncMapper;
import com.k12.resource.mapper.PrimaryChineseResourceMapper;
import com.k12.resource.mapper.StageSubjectBrowseMapper;
import com.k12.resource.mapper.SysSearchDocumentMapper;
import com.k12.resource.mapper.TopicResourceMapper;
import com.k12.resource.search.engine.SearchEngineSyncService;
import com.k12.resource.util.PublicResourceQuerySupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Stream;

/**
 * P1 全站统一索引同步：写入 sys_search_document
 */
@Slf4j
@Service
public class SearchDocumentSyncService {

    private static final Map<String, String> SUBJECT_CN_TO_KEY = Map.ofEntries(
            Map.entry("语文", "chinese"), Map.entry("数学", "math"), Map.entry("英语", "english"),
            Map.entry("物理", "physics"), Map.entry("化学", "chemistry"), Map.entry("生物", "biology"),
            Map.entry("历史", "history"), Map.entry("地理", "geography"), Map.entry("政治", "politics"),
            Map.entry("科学", "science"), Map.entry("美术", "art"), Map.entry("音乐", "music"),
            Map.entry("体育", "pe"), Map.entry("拼音识字", "chinese")
    );

    private static final Map<String, String> STAGE_CN_TO_KEY = Map.ofEntries(
            Map.entry("幼儿", "preschool"), Map.entry("学前", "preschool"),
            Map.entry("小学", "primary"), Map.entry("初中", "junior"),
            Map.entry("高中", "senior"), Map.entry("美术", "art"), Map.entry("舞蹈", "dance"),
            Map.entry("preschool", "preschool"), Map.entry("primary", "primary"),
            Map.entry("junior", "junior"), Map.entry("senior", "senior"),
            Map.entry("art", "art"), Map.entry("dance", "dance")
    );

    private static final Map<String, String> STAGE_KEY_TO_NAME = Map.of(
            "preschool", "幼儿", "primary", "小学", "junior", "初中",
            "senior", "高中", "art", "美术", "dance", "舞蹈"
    );

    private final SysSearchDocumentMapper documentMapper;
    private final PrimaryChineseResourceMapper primaryChineseResourceMapper;
    private final TopicResourceMapper topicResourceMapper;
    private final CompetitionResourceMapper competitionResourceMapper;
    private final CultureResourceMapper cultureResourceMapper;
    private final EduResourceSearchSyncMapper eduResourceSearchSyncMapper;
    private final ArticleSearchMapper articleSearchMapper;
    private final StageSubjectBrowseMapper stageSubjectBrowseMapper;
    private final SearchEngineSyncService engineSyncService;
    public SearchDocumentSyncService(SysSearchDocumentMapper documentMapper, PrimaryChineseResourceMapper primaryChineseResourceMapper, TopicResourceMapper topicResourceMapper, CompetitionResourceMapper competitionResourceMapper, CultureResourceMapper cultureResourceMapper, EduResourceSearchSyncMapper eduResourceSearchSyncMapper, ArticleSearchMapper articleSearchMapper, StageSubjectBrowseMapper stageSubjectBrowseMapper, SearchEngineSyncService engineSyncService) {
        this.documentMapper = documentMapper;
        this.primaryChineseResourceMapper = primaryChineseResourceMapper;
        this.topicResourceMapper = topicResourceMapper;
        this.competitionResourceMapper = competitionResourceMapper;
        this.cultureResourceMapper = cultureResourceMapper;
        this.eduResourceSearchSyncMapper = eduResourceSearchSyncMapper;
        this.articleSearchMapper = articleSearchMapper;
        this.stageSubjectBrowseMapper = stageSubjectBrowseMapper;
        this.engineSyncService = engineSyncService;
    }


    public long countDocuments() {
        return documentMapper.selectCount(
                new LambdaQueryWrapper<SysSearchDocument>().eq(SysSearchDocument::getIsDeleted, 0));
    }

    @Async
    public void rebuildAllAsync() {
        rebuildAll();
    }

    /** Phase 5-E：taxonomy 变更后增量刷新学段学科搜索入口 */
    @Async
    public void syncStageSubjectBrowseAsync() {
        int n = syncStageSubjectBrowse();
        log.info("stage-subject search documents refreshed, {} entries", n);
        engineSyncService.syncFullAsync();
    }

    public int rebuildAll() {
        log.info("开始全量重建 sys_search_document");
        documentMapper.delete(new LambdaQueryWrapper<>());
        int total = 0;
        total += syncStaticEntries();
        total += syncStageSubjectBrowse();
        total += syncEduResources();
        total += syncPrimaryChineseAll();
        total += syncTopicAll();
        total += syncCompetitionAll();
        total += syncCultureAll();
        total += syncArticles();
        log.info("sys_search_document 全量重建完成，共 {} 条", total);
        engineSyncService.syncFullAsync();
        return total;
    }

    @Async
    public void syncPrimaryByIdAsync(Long id) {
        syncPrimaryById(id);
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

    @Async
    public void syncArticleByIdAsync(Long id) {
        syncArticleById(id);
    }

    public void syncArticleById(Long id) {
        if (id == null) return;
        Article a = articleSearchMapper.selectById(id);
        if (a == null || !Integer.valueOf(1).equals(a.getStatus()) || Integer.valueOf(1).equals(a.getDeleted())) {
            deleteDoc("news", String.valueOf(id));
            return;
        }
        fillArticleCategoryName(a);
        upsert(toArticleDoc(a));
    }

    @Async
    public void syncEduResourceByIdAsync(Long id) {
        if (id == null) return;
        List<EduResourceSearchRow> rows = eduResourceSearchSyncMapper.selectPublishedRows().stream()
                .filter(r -> id.equals(r.getResourceId())).toList();
        if (rows.isEmpty()) {
            deleteDoc("resource", String.valueOf(id));
            return;
        }
        for (EduResourceSearchRow row : rows) {
            upsert(toEduResourceDoc(row));
        }
    }

    public void syncPrimaryById(Long id) {
        if (id == null) return;
        PrimaryChineseResource r = primaryChineseResourceMapper.selectById(id);
        if (r == null || !PublicResourceQuerySupport.isPubliclyPublished(r)) {
            deleteDoc("primary_chinese", String.valueOf(id));
            return;
        }
        upsert(toPrimaryDoc(r));
    }

    public void syncTopicById(Long id) {
        if (id == null) return;
        TopicResource r = topicResourceMapper.selectById(id);
        if (r == null || !Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("topic", String.valueOf(id));
            return;
        }
        upsert(toTopicDoc(r));
    }

    public void syncCompetitionById(Long id) {
        if (id == null) return;
        CompetitionResource r = competitionResourceMapper.selectById(id);
        if (r == null || !Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("competition", String.valueOf(id));
            return;
        }
        upsert(toCompetitionDoc(r));
    }

    public void syncCultureById(Long id) {
        if (id == null) return;
        CultureResource r = cultureResourceMapper.selectById(id);
        if (r == null || !Integer.valueOf(1).equals(r.getStatus()) || Integer.valueOf(1).equals(r.getDeleted())) {
            deleteDoc("culture", String.valueOf(id));
            return;
        }
        upsert(toCultureDoc(r));
    }

    private int syncStaticEntries() {
        int n = 0;
        n += upsert(staticDoc("channel:theme-class-meeting", "channel", "theme-class-meeting",
                "主题班会", "特色频道 / 德育", "主题班会频道入口", "班会,德育,成长",
                null, null, null, null, null, null, "class_meeting", "特色频道",
                null, null, null, null, "/theme-class-meeting", null, 10.0));
        n += upsert(staticDoc("channel:career", "feature", "career",
                "生涯规划", "特色频道 / 升学指导", "生涯规划频道", "生涯,志愿,升学",
                null, null, null, null, null, null, "career", "特色频道",
                null, null, null, null, "/topic", null, 10.0));
        n += upsert(staticDoc("channel:culture", "channel", "culture",
                "传统文化", "特色频道 / 国学", "传统文化频道", "传统文化,国学,诗词",
                null, null, null, null, null, null, "culture", "传统文化",
                null, null, null, null, "/culture", null, 10.0));
        n += upsert(staticDoc("channel:competition", "channel", "competition",
                "竞赛专区", "特色频道 / 奥赛", "竞赛专区频道", "竞赛,奥赛,奥数",
                null, null, null, null, null, null, "competition", "竞赛专区",
                null, null, null, null, "/competition", null, 10.0));
        n += upsert(staticDoc("channel:topic", "channel", "topic",
                "专题资源", "特色频道 / 专题", "专题资源频道", "专题,备考,冲刺",
                null, null, null, null, null, null, "topic", "专题资源",
                null, null, null, null, "/topic", null, 10.0));
        n += upsert(staticDoc("prep:lesson", "prep", "lesson",
                "备课中心", "备课专区 / 教案课件", "备课中心入口", "备课,教案,课件,学案",
                null, null, null, null, null, null, "prep", "备课专区",
                null, null, null, null, "/lesson", null, 10.0));
        n += upsert(staticDoc("prep:smart", "prep", "smart-lesson",
                "智能备课", "备课专区 / AI", "智能备课入口", "智能备课,AI",
                null, null, null, null, null, null, "prep", "备课专区",
                null, null, null, null, "/lesson/smart", null, 10.0));
        n += upsert(staticDoc("prep:assemble", "prep", "assemble",
                "组卷", "备课专区 / 试卷", "智能组卷入口", "组卷,试卷,出题",
                null, null, null, null, null, null, "prep", "备课专区",
                null, null, null, null, "/lesson/assemble", null, 10.0));
        n += upsert(staticDoc("news:hub", "page", "news",
                "教育资讯", "资讯频道入口", "教育资讯首页", "资讯,新闻,政策,通知",
                null, null, null, null, null, null, "news", "教育资讯",
                null, null, null, null, "/news", null, 10.0));
        n += upsert(staticDoc("news:notice", "page", "notice",
                "教育局通知", "教育资讯 / 通知", "教育局通知栏目", "教育局,通知,公告",
                null, null, null, null, null, null, "news", "教育资讯",
                null, null, null, null, "/news/list?keyword=通知", null, 9.0));
        n += upsert(staticDoc("page:help", "page", "help",
                "帮助中心", "系统帮助", "帮助中心", "帮助,FAQ,使用说明",
                null, null, null, null, null, null, null, null,
                null, null, null, null, "/help", null, 5.0));
        return n;
    }

    private int syncStageSubjectBrowse() {
        int n = 0;
        for (StageSubjectRow row : stageSubjectBrowseMapper.selectStageSubjects()) {
            if (!StringUtils.hasText(row.getStageKey()) || !StringUtils.hasText(row.getSubjectKey())) continue;
            String label = row.getStageName() + row.getSubjectName();
            String docId = "subject:" + row.getStageKey() + ":" + row.getSubjectKey();
            String route = "/subject/" + row.getStageKey() + "/" + row.getSubjectKey() + "/tongbian2024";
            String subtitle = row.getStageName() + " / " + row.getSubjectName() + " / 同步资源";
            n += upsert(staticDoc(docId, "subject", row.getStageKey() + ":" + row.getSubjectKey(),
                    label, subtitle, subtitle, label + "," + row.getSubjectName(),
                    row.getStageKey(), row.getStageName(), row.getSubjectKey(), row.getSubjectName(),
                    null, null, "stage_resource", "学段资源",
                    null, null, null, null, route, null, 8.0));
        }
        return n;
    }

    private int syncEduResources() {
        List<EduResourceSearchRow> rows = eduResourceSearchSyncMapper.selectPublishedRows();
        for (EduResourceSearchRow row : rows) {
            upsert(toEduResourceDoc(row));
        }
        return rows.size();
    }

    private int syncArticles() {
        List<Article> articles = articleSearchMapper.selectPublishedAll();
        for (Article a : articles) {
            upsert(toArticleDoc(a));
        }
        return articles.size();
    }

    private int syncPrimaryChineseAll() {
        LambdaQueryWrapper<PrimaryChineseResource> w = new LambdaQueryWrapper<>();
        w.eq(PrimaryChineseResource::getIsDeleted, 0)
                .and(q -> q.eq(PrimaryChineseResource::getPublishStatus, 1)
                        .or(o -> o.isNull(PrimaryChineseResource::getPublishStatus)
                                .eq(PrimaryChineseResource::getStatus, 1)));
        List<PrimaryChineseResource> list = primaryChineseResourceMapper.selectList(w);
        for (PrimaryChineseResource r : list) upsert(toPrimaryDoc(r));
        return list.size();
    }

    private int syncTopicAll() {
        LambdaQueryWrapper<TopicResource> w = new LambdaQueryWrapper<>();
        w.eq(TopicResource::getDeleted, 0).eq(TopicResource::getStatus, 1);
        List<TopicResource> list = topicResourceMapper.selectList(w);
        for (TopicResource r : list) upsert(toTopicDoc(r));
        return list.size();
    }

    private int syncCompetitionAll() {
        LambdaQueryWrapper<CompetitionResource> w = new LambdaQueryWrapper<>();
        w.eq(CompetitionResource::getDeleted, 0).eq(CompetitionResource::getStatus, 1);
        List<CompetitionResource> list = competitionResourceMapper.selectList(w);
        for (CompetitionResource r : list) upsert(toCompetitionDoc(r));
        return list.size();
    }

    private int syncCultureAll() {
        LambdaQueryWrapper<CultureResource> w = new LambdaQueryWrapper<>();
        w.eq(CultureResource::getDeleted, 0).eq(CultureResource::getStatus, 1);
        List<CultureResource> list = cultureResourceMapper.selectList(w);
        for (CultureResource r : list) upsert(toCultureDoc(r));
        return list.size();
    }

    private SysSearchDocument toEduResourceDoc(EduResourceSearchRow r) {
        NormalizedResourceType type = normalizeResourceType(r.getResourceTypeName());
        String summary = truncate(defaultText(r.getDescription(), r.getTitle()), 1000);
        String keywords = joinText(r.getModuleName(), type.displayName(), r.getUnitName(), r.getEditionName());
        String content = joinText(r.getDescription(), r.getUnitName(), r.getModuleName());
        String subtitle = joinSlash(r.getStageName(), r.getSubjectName(), type.displayName());
        return buildDoc("resource:" + r.getResourceId(), "resource", String.valueOf(r.getResourceId()),
                r.getTitle(), subtitle, summary, content, keywords,
                r.getStageKey(), r.getStageName(), r.getSubjectKey(), r.getSubjectName(),
                null, r.getGradeName(), "stage_resource", "学段资源",
                null, r.getModuleName(), type.key(), type.displayName(),
                "/resource/" + r.getResourceId(), null,
                r.getDownloadCount(), r.getViewCount(), r.getUploadTime(), 0, calcHot(r.getDownloadCount(), r.getViewCount()));
    }

    private record NormalizedResourceType(String key, String displayName) {}

    /** 教学设计/PPT 等归一化到标准 type_key */
    private NormalizedResourceType normalizeResourceType(String raw) {
        if (!StringUtils.hasText(raw)) {
            return new NormalizedResourceType(null, raw);
        }
        String t = raw.trim();
        if (t.contains("教学设计") || "教案".equals(t) || t.contains("教案")) {
            return new NormalizedResourceType("lesson_plan", "教案");
        }
        if (t.contains("课件") || "ppt".equalsIgnoreCase(t) || "PPT".equals(t)) {
            return new NormalizedResourceType("courseware", "课件");
        }
        if (t.contains("试卷") || t.contains("试题")) {
            return new NormalizedResourceType("exam_paper", "试卷");
        }
        if (t.contains("学案") || t.contains("导学案")) {
            return new NormalizedResourceType("study_guide", "学案");
        }
        return new NormalizedResourceType(null, t);
    }

    private String buildPrimaryDetailRoute(PrimaryChineseResource r) {
        String stageKey = resolveStageKey(r.getStage());
        String subjectKey = resolveSubjectKey(r.getSubject());
        StringBuilder q = new StringBuilder("from=subject&version=tongbian2024");
        if (StringUtils.hasText(stageKey)) {
            q.append("&stage=").append(urlEncode(stageKey));
        }
        if (StringUtils.hasText(subjectKey)) {
            q.append("&subject=").append(urlEncode(subjectKey));
        }
        if (StringUtils.hasText(r.getBrandCode())) {
            q.append("&brand=").append(urlEncode(r.getBrandCode()));
        }
        if (StringUtils.hasText(r.getModule())) {
            q.append("&module=").append(urlEncode(r.getModule()));
        }
        if (StringUtils.hasText(r.getType())) {
            q.append("&type=").append(urlEncode(r.getType()));
        }
        if (StringUtils.hasText(r.getUnitName())) {
            q.append("&unit=").append(urlEncode(r.getUnitName()));
        }
        if (StringUtils.hasText(r.getLessonName())) {
            q.append("&lesson=").append(urlEncode(r.getLessonName()));
        }
        if (r.getCatalogNodeId() != null && r.getCatalogNodeId() > 0) {
            q.append("&catalogNodeId=").append(r.getCatalogNodeId());
        }
        return "/resource/" + r.getId() + "?" + q;
    }

    private String resolveSubjectKey(String subject) {
        if (!StringUtils.hasText(subject)) return null;
        String trimmed = subject.trim();
        String key = SUBJECT_CN_TO_KEY.get(trimmed);
        if (key != null) return key;
        return trimmed.toLowerCase(Locale.ROOT);
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private SysSearchDocument toPrimaryDoc(PrimaryChineseResource r) {
        String stageKey = resolveStageKey(r.getStage());
        String stageName = STAGE_KEY_TO_NAME.getOrDefault(stageKey, r.getStage());
        NormalizedResourceType type = normalizeResourceType(r.getType());
        String summary = truncate(defaultText(r.getRemark(), r.getLessonName()), 1000);
        String content = joinText(r.getRemark(), r.getLessonName(), r.getCatalogPath(), r.getUnitName());
        String keywords = joinText(r.getModule(), type.displayName(), r.getEdition(), r.getSubject(), r.getGradeName());
        String subtitle = joinSlash(stageName, r.getSubject(), type.displayName());
        return buildDoc("primary_chinese:" + r.getId(), "resource", String.valueOf(r.getId()),
                r.getTitle(), subtitle, summary, content, keywords,
                stageKey, stageName, null, r.getSubject(),
                null, r.getGradeName(), "stage_resource", "学段资源",
                null, r.getModule(), type.key(), type.displayName(),
                buildPrimaryDetailRoute(r), null,
                r.getDownloadCount(), r.getViewCount(), r.getUploadTime(), 0,
                calcHot(r.getDownloadCount(), r.getViewCount()));
    }

    private SysSearchDocument toTopicDoc(TopicResource r) {
        String summary = truncate(defaultText(r.getSummary(), r.getTopicLabel()), 1000);
        String content = joinText(r.getSummary(), r.getTags(), r.getTopicLabel());
        String keywords = joinText(r.getTags(), r.getCategory(), r.getSubject(), r.getResourceForm());
        return buildDoc("topic:" + r.getId(), "resource", String.valueOf(r.getId()),
                r.getTitle(), r.getTopicLabel(), summary, content, keywords,
                null, null, null, r.getSubject(),
                r.getGradeStage(), null, "topic", "专题资源",
                null, r.getCategory(), null, r.getResourceForm(),
                "/topic-zone/resource/" + r.getId(), r.getCoverUrl(),
                r.getDownloadCount(), r.getViewCount(), r.getCreateTime(), vipFlag(r.getIsFree(), r.getIsElite()),
                calcHot(r.getDownloadCount(), r.getViewCount()));
    }

    private SysSearchDocument toCompetitionDoc(CompetitionResource r) {
        String stageKey = resolveStageKey(r.getGradeStage());
        String stageName = STAGE_KEY_TO_NAME.getOrDefault(stageKey, r.getGradeStage());
        String summary = truncate(defaultText(r.getSummary(), r.getCompetitionName()), 1000);
        String content = joinText(r.getSummary(), r.getTags(), r.getCompetitionName());
        String keywords = joinText(r.getTags(), r.getSubject(), r.getResourceForm());
        return buildDoc("competition:" + r.getId(), "resource", String.valueOf(r.getId()),
                r.getTitle(), r.getCompetitionName(), summary, content, keywords,
                stageKey, stageName, null, r.getSubject(),
                null, null, "competition", "竞赛专区",
                null, r.getCategory(), null, r.getResourceForm(),
                "/competition-zone/resource/" + r.getId(), r.getCoverUrl(),
                r.getDownloadCount(), r.getViewCount(), r.getCreateTime(), vipFlag(r.getIsFree(), r.getIsElite()),
                calcHot(r.getDownloadCount(), r.getViewCount()));
    }

    private SysSearchDocument toCultureDoc(CultureResource r) {
        String summary = truncate(defaultText(r.getSummary(), r.getLocation()), 1000);
        String content = joinText(r.getSummary(), r.getTags(), r.getLocation(), r.getCategory());
        String keywords = joinText(r.getTags(), r.getCategory(), r.getResourceKind());
        return buildDoc("culture:" + r.getId(), "resource", String.valueOf(r.getId()),
                r.getTitle(), r.getLocation(), summary, content, keywords,
                null, null, null, null,
                null, null, "culture", "传统文化",
                null, r.getCategory(), null, r.getResourceKind(),
                "/culture", r.getCoverUrl(),
                r.getDownloadCount(), r.getViewCount(), r.getCreateTime(), vipFlag(r.getIsFree(), r.getIsElite()),
                calcHot(r.getDownloadCount(), r.getViewCount()));
    }

    private SysSearchDocument toArticleDoc(Article a) {
        String summary = truncate(defaultText(a.getSummary(), a.getTitle()), 1000);
        String content = truncate(stripHtml(a.getContent()), 8000);
        String keywords = joinText(a.getTags(), a.getCategoryName(), a.getSubCategory(), a.getSourceName());
        String subtitle = joinSlash("教育资讯", a.getCategoryName(), a.getSubCategory());
        String route = "/news/" + a.getId();
        if ("notice".equalsIgnoreCase(a.getCategory()) || containsNotice(a.getTitle(), a.getCategoryName())) {
            keywords = joinText(keywords, "教育局,通知,公告");
        }
        return buildDoc("news:" + a.getId(), "news", String.valueOf(a.getId()),
                a.getTitle(), subtitle, summary, content, keywords,
                null, null, null, null,
                null, null, "news", "教育资讯",
                a.getCategory(), a.getCategoryName(), null, null,
                route, a.getCoverUrl(),
                0, a.getViewCount(), a.getPublishTime(), 0,
                calcHot(0, a.getViewCount()) + 2);
    }

    private SysSearchDocument staticDoc(
            String docId, String docType, String bizId, String title, String subtitle,
            String summary, String keywords,
            String stageKey, String stageName, String subjectKey, String subjectName,
            String gradeName, String editionName, String channelKey, String channelName,
            String moduleKey, String moduleName, String resourceTypeKey, String resourceTypeName,
            String route, String cover, Double hot) {
        return buildDoc(docId, docType, bizId, title, subtitle, summary, summary, keywords,
                stageKey, stageName, subjectKey, subjectName,
                null, gradeName, channelKey, channelName,
                moduleKey, moduleName, resourceTypeKey, resourceTypeName,
                route, cover, 0, 0, LocalDateTime.now(), 0, hot);
    }

    private SysSearchDocument buildDoc(
            String docId, String docType, String bizId, String title, String subtitle,
            String summary, String contentText, String keywordText,
            String stageKey, String stageName, String subjectKey, String subjectName,
            String gradeKey, String gradeName, String channelKey, String channelName,
            String moduleKey, String moduleName, String resourceTypeKey, String resourceTypeName,
            String routePath, String coverUrl,
            Integer downloadCount, Integer viewCount, LocalDateTime publishTime,
            int vipFlag, double hotScore) {
        SysSearchDocument doc = new SysSearchDocument();
        doc.setDocId(docId);
        doc.setDocType(docType);
        doc.setBizId(bizId);
        doc.setTitle(truncate(title, 255));
        doc.setSubtitle(truncate(subtitle, 255));
        doc.setSummary(summary);
        doc.setContentText(contentText);
        doc.setKeywordText(keywordText);
        doc.setStageKey(stageKey);
        doc.setStageName(stageName);
        doc.setSubjectKey(subjectKey);
        doc.setSubjectName(subjectName);
        doc.setGradeKey(gradeKey);
        doc.setGradeName(gradeName);
        doc.setChannelKey(channelKey);
        doc.setChannelName(channelName);
        doc.setModuleKey(moduleKey);
        doc.setModuleName(moduleName);
        doc.setResourceTypeKey(resourceTypeKey);
        doc.setResourceTypeName(resourceTypeName);
        doc.setRoutePath(routePath);
        doc.setCoverUrl(coverUrl);
        doc.setDownloadCount(nvl(downloadCount));
        doc.setViewCount(nvl(viewCount));
        doc.setPublishTime(publishTime);
        doc.setQualityScore(BigDecimal.valueOf("resource".equals(docType) ? 1 : 2));
        doc.setHotScore(hotScore);
        doc.setVipFlag(vipFlag);
        doc.setStatus(1);
        doc.setIsDeleted(0);
        return doc;
    }

    private int upsert(SysSearchDocument doc) {
        if (doc == null || !StringUtils.hasText(doc.getDocId())) return 0;
        SysSearchDocument existing = documentMapper.selectOne(
                new LambdaQueryWrapper<SysSearchDocument>().eq(SysSearchDocument::getDocId, doc.getDocId()));
        if (existing == null) {
            documentMapper.insert(doc);
        } else {
            doc.setId(existing.getId());
            documentMapper.updateById(doc);
        }
        engineSyncService.syncDocument(doc);
        return 1;
    }

    private void deleteDoc(String typePrefix, String bizId) {
        String docId = typePrefix + ":" + bizId;
        documentMapper.delete(new LambdaQueryWrapper<SysSearchDocument>()
                .eq(SysSearchDocument::getDocId, docId));
        engineSyncService.deleteByDocId(docId);
    }

    private String resolveStageKey(String stage) {
        if (!StringUtils.hasText(stage)) return null;
        String key = STAGE_CN_TO_KEY.get(stage.trim());
        if (key != null) return key;
        String lower = stage.trim().toLowerCase(Locale.ROOT);
        return STAGE_CN_TO_KEY.getOrDefault(lower, lower);
    }

    private int vipFlag(Integer isFree, Integer isElite) {
        if (Integer.valueOf(0).equals(isFree) || Integer.valueOf(1).equals(isElite)) return 1;
        return 0;
    }

    private double calcHot(Integer downloadCount, Integer viewCount) {
        return Math.log1p(nvl(downloadCount)) * 2 + Math.log1p(nvl(viewCount));
    }

    private boolean containsNotice(String title, String categoryName) {
        String t = (title == null ? "" : title) + (categoryName == null ? "" : categoryName);
        return t.contains("通知") || t.contains("公告") || t.contains("教育局");
    }

    private String stripHtml(String html) {
        if (!StringUtils.hasText(html)) return "";
        return html.replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
    }

    private String joinSlash(String... parts) {
        return Stream.of(parts).filter(StringUtils::hasText).reduce((a, b) -> a + " / " + b).orElse("");
    }

    private String joinText(String... parts) {
        return Stream.of(parts).filter(StringUtils::hasText).reduce((a, b) -> a + " " + b).orElse("");
    }

    private String defaultText(String primary, String fallback) {
        return StringUtils.hasText(primary) ? primary : (StringUtils.hasText(fallback) ? fallback : "");
    }

    private String truncate(String text, int max) {
        if (!StringUtils.hasText(text) || text.length() <= max) return text;
        return text.substring(0, max);
    }

    private int nvl(Integer v) {
        return v == null ? 0 : v;
    }

    private void fillArticleCategoryName(Article a) {
        if (a == null || StringUtils.hasText(a.getCategoryName()) || !StringUtils.hasText(a.getCategory())) {
            return;
        }
        Map<String, String> names = Map.of(
                "policy", "教育政策",
                "reform", "教学改革",
                "research", "教研动态",
                "teacher", "名师讲堂",
                "exam", "升学备考"
        );
        a.setCategoryName(names.getOrDefault(a.getCategory(), "教育资讯"));
    }
}
