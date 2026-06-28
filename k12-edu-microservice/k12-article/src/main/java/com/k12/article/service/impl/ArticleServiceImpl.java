package com.k12.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.BusinessException;
import com.k12.common.PageResult;
import com.k12.common.dto.AdminArticleQueryDTO;
import com.k12.common.dto.ArticleCreateDTO;
import com.k12.common.dto.ArticleDetailVO;
import com.k12.common.dto.ArticleQueryDTO;
import com.k12.common.dto.ConsultLeadDTO;
import com.k12.common.dto.NewsHomeVO;
import com.k12.common.dto.RelatedLinkVO;
import com.k12.common.entity.Article;
import com.k12.common.entity.ArticleCollect;
import com.k12.common.entity.ConsultLead;
import com.k12.article.client.ArticleSearchIndexClient;
import com.k12.article.mapper.ArticleCollectMapper;
import com.k12.article.mapper.ArticleMapper;
import com.k12.article.mapper.ConsultLeadMapper;
import com.k12.article.service.ArticleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("null")
public class ArticleServiceImpl implements ArticleService {

    private static final List<String> CHANNEL_KEYS = List.of(
            "policy", "reform", "research", "teacher", "exam"
    );

    private static final List<String> HOT_KEYWORDS = List.of(
            "\u6210\u90fd\u4e2d\u8003", "\u7ef5\u9633\u4e2d\u8003", "\u62db\u751f\u653f\u7b56", "\u671f\u4e2d\u590d\u4e60", "\u9ad8\u8003\u51b2\u523a",
            "\u540d\u5e08\u8bb2\u5802", "\u65b0\u9ad8\u8003\u9009\u79d1", "\u6691\u5047\u4f5c\u4e1a", "\u6559\u7814\u52a8\u6001", "\u5347\u5b66\u89c4\u5212"
    );

    private final ArticleMapper articleMapper;
    private final ArticleCollectMapper articleCollectMapper;
    private final ConsultLeadMapper consultLeadMapper;
    private final ArticleSearchIndexClient articleSearchIndexClient;

    public ArticleServiceImpl(
            ArticleMapper articleMapper,
            ArticleCollectMapper articleCollectMapper,
            ConsultLeadMapper consultLeadMapper,
            ArticleSearchIndexClient articleSearchIndexClient) {
        this.articleMapper = articleMapper;
        this.articleCollectMapper = articleCollectMapper;
        this.consultLeadMapper = consultLeadMapper;
        this.articleSearchIndexClient = articleSearchIndexClient;
    }

    @Override
    public PageResult<Article> listArticles(ArticleQueryDTO query) {
        LambdaQueryWrapper<Article> wrapper = buildListWrapper(query);
        Page<Article> page = new Page<>(query.getCurrent(), query.getSize());
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        fillCategoryNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public ArticleDetailVO getDetail(Long id, Long userId) {
        Article article = articleMapper.selectById(id);
        if (article == null || article.getStatus() == null || article.getStatus() != 1) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728\u6216\u5df2\u4e0b\u7ebf");
        }
        article.setViewCount((article.getViewCount() == null ? 0 : article.getViewCount()) + 1);
        articleMapper.updateById(article);
        fillCategoryNames(List.of(article));

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setArticle(article);
        vo.setRelatedArticles(findRelatedArticles(article, 4));
        vo.setRelatedLinks(buildRelatedLinks(article));
        vo.setCollected(userId != null && isCollected(userId, id));
        return vo;
    }

    @Override
    public NewsHomeVO getHome() {
        NewsHomeVO vo = new NewsHomeVO();
        vo.setHotKeywords(HOT_KEYWORDS);

        LambdaQueryWrapper<Article> topWrapper = new LambdaQueryWrapper<>();
        topWrapper.eq(Article::getStatus, 1)
                .eq(Article::getIsTop, 1)
                .orderByAsc(Article::getTopOrder)
                .orderByDesc(Article::getPublishTime)
                .last("LIMIT 5");
        List<Article> headlines = articleMapper.selectList(topWrapper);
        if (headlines.isEmpty()) {
            headlines = articleMapper.selectList(new LambdaQueryWrapper<Article>()
                    .eq(Article::getStatus, 1)
                    .orderByDesc(Article::getViewCount)
                    .last("LIMIT 5"));
        }
        fillCategoryNames(headlines);
        vo.setHeadlines(headlines);

        Map<String, List<Article>> channels = new LinkedHashMap<>();
        for (String key : CHANNEL_KEYS) {
            LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
            w.eq(Article::getStatus, 1)
                    .eq(Article::getCategory, key)
                    .orderByDesc(Article::getPublishTime)
                    .last("LIMIT 6");
            List<Article> list = articleMapper.selectList(w);
            fillCategoryNames(list);
            channels.put(key, list);
        }
        vo.setChannels(channels);
        return vo;
    }

    @Override
    public PageResult<Article> search(String keyword, Integer current, Integer size) {
        ArticleQueryDTO q = new ArticleQueryDTO();
        q.setKeyword(keyword);
        q.setCurrent(current == null ? 1 : current);
        q.setSize(size == null ? 10 : size);
        q.setSort("publishTime");
        return listArticles(q);
    }

    @Override
    public void collect(Long userId, Long articleId) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        if (articleMapper.selectById(articleId) == null) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        if (isCollected(userId, articleId)) {
            return;
        }
        ArticleCollect c = new ArticleCollect();
        c.setUserId(userId);
        c.setArticleId(articleId);
        articleCollectMapper.insert(c);
    }

    @Override
    public void uncollect(Long userId, Long articleId) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        articleCollectMapper.delete(new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getArticleId, articleId));
    }

    @Override
    public boolean isCollected(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            return false;
        }
        return articleCollectMapper.selectCount(new LambdaQueryWrapper<ArticleCollect>()
                .eq(ArticleCollect::getUserId, userId)
                .eq(ArticleCollect::getArticleId, articleId)) > 0;
    }

    @Override
    public Long createArticle(ArticleCreateDTO dto, Long userId, String username) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        Article article = new Article();
        article.setTitle(dto.getTitle().trim());
        article.setSummary(StringUtils.hasText(dto.getSummary()) ? dto.getSummary().trim() : "");
        article.setContent(dto.getContent());
        article.setCoverUrl(dto.getCoverUrl());
        article.setCategory(dto.getCategory());
        article.setTags(dto.getTags());
        article.setGradeLevels(StringUtils.hasText(dto.getGradeLevels()) ? dto.getGradeLevels() : "all");
        article.setRegions(StringUtils.hasText(dto.getRegions()) ? dto.getRegions() : "national");
        article.setPolicyPoints(dto.getPolicyPoints());
        article.setRelatedTopicKeywords(dto.getRelatedTopicKeywords());
        article.setConsultEnabled(dto.getConsultEnabled() != null ? dto.getConsultEnabled() : 1);
        article.setContentType(StringUtils.hasText(dto.getContentType()) ? dto.getContentType() : "article");
        int status = dto.getStatus() != null ? dto.getStatus() : 2;
        article.setStatus(status);
        if (status == 1) {
            article.setPublishTime(dto.getPublishTime() != null ? dto.getPublishTime() : java.time.LocalDateTime.now());
        }
        article.setAuthor(StringUtils.hasText(dto.getAuthor()) ? dto.getAuthor().trim()
                : (StringUtils.hasText(username) ? username : "\u7528\u6237" + userId));
        article.setViewCount(0);
        article.setCommentCount(0);
        article.setLikeCount(0);
        article.setIsTop(dto.getIsTop() != null ? dto.getIsTop() : 0);
        article.setTopOrder(dto.getTopOrder());
        article.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : 0);
        fillCategoryNames(List.of(article));
        articleMapper.insert(article);
        articleSearchIndexClient.syncArticleAsync(article.getId());
        return article.getId();
    }

    @Override
    public PageResult<Article> listAdminArticles(AdminArticleQueryDTO query) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getCategory())) {
            wrapper.eq(Article::getCategory, query.getCategory());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Article::getStatus, query.getStatus());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Article::getTitle, kw)
                    .or().like(Article::getSummary, kw)
                    .or().like(Article::getTags, kw));
        }
        wrapper.orderByDesc(Article::getPublishTime).orderByDesc(Article::getCreateTime);
        Page<Article> page = new Page<>(query.getCurrent(), query.getSize());
        Page<Article> result = articleMapper.selectPage(page, wrapper);
        fillCategoryNames(result.getRecords());
        return new PageResult<>(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    public Article getAdminArticle(Long id) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        fillCategoryNames(List.of(article));
        return article;
    }

    @Override
    public void updateArticle(Long id, ArticleCreateDTO dto) {
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        article.setTitle(dto.getTitle().trim());
        if (dto.getSummary() != null) {
            article.setSummary(dto.getSummary().trim());
        }
        if (dto.getContent() != null) {
            article.setContent(dto.getContent());
        }
        if (dto.getCoverUrl() != null) {
            article.setCoverUrl(dto.getCoverUrl());
        }
        if (StringUtils.hasText(dto.getCategory())) {
            article.setCategory(dto.getCategory());
        }
        if (dto.getAuthor() != null) {
            article.setAuthor(dto.getAuthor().trim());
        }
        if (dto.getTags() != null) {
            article.setTags(dto.getTags());
        }
        if (dto.getGradeLevels() != null) {
            article.setGradeLevels(dto.getGradeLevels());
        }
        if (dto.getRegions() != null) {
            article.setRegions(dto.getRegions());
        }
        if (dto.getPolicyPoints() != null) {
            article.setPolicyPoints(dto.getPolicyPoints());
        }
        if (dto.getRelatedTopicKeywords() != null) {
            article.setRelatedTopicKeywords(dto.getRelatedTopicKeywords());
        }
        if (dto.getConsultEnabled() != null) {
            article.setConsultEnabled(dto.getConsultEnabled());
        }
        if (dto.getContentType() != null) {
            article.setContentType(dto.getContentType());
        }
        if (dto.getStatus() != null) {
            article.setStatus(dto.getStatus());
            if (dto.getStatus() == 1 && article.getPublishTime() == null) {
                article.setPublishTime(java.time.LocalDateTime.now());
            }
        }
        if (dto.getPublishTime() != null) {
            article.setPublishTime(dto.getPublishTime());
        }
        if (dto.getIsTop() != null) {
            article.setIsTop(dto.getIsTop());
        }
        if (dto.getTopOrder() != null) {
            article.setTopOrder(dto.getTopOrder());
        }
        if (dto.getIsFeatured() != null) {
            article.setIsFeatured(dto.getIsFeatured());
        }
        fillCategoryNames(List.of(article));
        articleMapper.updateById(article);
        articleSearchIndexClient.syncArticleAsync(id);
    }

    @Override
    public void updateArticleStatus(Long id, int status) {
        if (status < 0 || status > 3) {
            throw new BusinessException("无效的资讯状态");
        }
        Article article = articleMapper.selectById(id);
        if (article == null) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        article.setStatus(status);
        if (status == 1 && article.getPublishTime() == null) {
            article.setPublishTime(java.time.LocalDateTime.now());
        }
        articleMapper.updateById(article);
        articleSearchIndexClient.syncArticleAsync(id);
    }

    @Override
    public void deleteArticle(Long id) {
        if (articleMapper.selectById(id) == null) {
            throw new BusinessException("\u6587\u7ae0\u4e0d\u5b58\u5728");
        }
        articleMapper.deleteById(id);
        articleSearchIndexClient.syncArticleAsync(id);
    }

    @Override
    public void submitConsultLead(ConsultLeadDTO dto, Long userId) {
        ConsultLead lead = new ConsultLead();
        lead.setArticleId(dto.getArticleId());
        lead.setUserId(userId);
        lead.setName(dto.getName().trim());
        lead.setPhone(dto.getPhone().trim());
        lead.setGrade(dto.getGrade());
        lead.setIntentType(StringUtils.hasText(dto.getIntentType()) ? dto.getIntentType() : "general");
        lead.setRemark(dto.getRemark());
        lead.setSourcePage(dto.getSourcePage());
        lead.setStatus(0);
        consultLeadMapper.insert(lead);
    }

    private LambdaQueryWrapper<Article> buildListWrapper(ArticleQueryDTO query) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getCategory()) && !"all".equalsIgnoreCase(query.getCategory())) {
            wrapper.eq(Article::getCategory, query.getCategory());
        }
        if (StringUtils.hasText(query.getGradeLevel()) && !"all".equalsIgnoreCase(query.getGradeLevel())) {
            wrapper.and(w -> w.eq(Article::getGradeLevels, "all")
                    .or().like(Article::getGradeLevels, query.getGradeLevel()));
        }
        if (StringUtils.hasText(query.getRegion()) && !"all".equalsIgnoreCase(query.getRegion())) {
            wrapper.and(w -> w.eq(Article::getRegions, "national")
                    .or().eq(Article::getRegions, query.getRegion())
                    .or().like(Article::getRegions, query.getRegion()));
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword().trim();
            wrapper.and(w -> w.like(Article::getTitle, kw)
                    .or().like(Article::getSummary, kw)
                    .or().like(Article::getTags, kw));
        }
        wrapper.eq(Article::getStatus, 1);
        if ("hot".equalsIgnoreCase(query.getSort())) {
            wrapper.orderByDesc(Article::getViewCount);
        } else {
            wrapper.orderByDesc(Article::getPublishTime).orderByDesc(Article::getCreateTime);
        }
        return wrapper;
    }

    private List<Article> findRelatedArticles(Article article, int limit) {
        LambdaQueryWrapper<Article> w = new LambdaQueryWrapper<>();
        w.eq(Article::getStatus, 1)
                .eq(Article::getCategory, article.getCategory())
                .ne(Article::getId, article.getId())
                .orderByDesc(Article::getViewCount)
                .last("LIMIT " + limit);
        List<Article> list = articleMapper.selectList(w);
        fillCategoryNames(list);
        return list;
    }

    private List<RelatedLinkVO> buildRelatedLinks(Article article) {
        List<RelatedLinkVO> links = new ArrayList<>();
        String kw = article.getRelatedTopicKeywords();
        if (StringUtils.hasText(kw)) {
            String first = kw.split(",")[0].trim();
            links.add(buildLink(
                    "\u4e13\u9898\uff1a" + first,
                    "/topic-zone?keyword=" + encodeKeyword(first),
                    "topic",
                    "\ud83d\udcda"
            ));
        }
        String cat = article.getCategory();
        if ("exam".equals(cat)) {
            links.add(buildLink("\u5347\u5b66\u5907\u8003\u4e13\u9898\u8d44\u6e90", "/topic-zone?keyword=\u5347\u5b66\u51b2\u523a", "topic", "\ud83c\udfc6"));
            links.add(buildLink("\u751f\u6daf\u89c4\u5212\u9891\u9053", "/feature/shengya", "feature", "\ud83d\udccd"));
        } else if ("teacher".equals(cat)) {
            links.add(buildLink("\u7ade\u8d5b\u4e13\u533a", "/competition-zone", "feature", "\ud83c\udfc5"));
        } else if ("policy".equals(cat)) {
            links.add(buildLink("\u6210\u90fd\u7247\u533a\u4e13\u9898", "/topic-zone?keyword=\u6210\u90fd", "topic", "\ud83d\udccd"));
        }
        links.add(buildLink("\u67e5\u770b\u66f4\u591a\u8d44\u8baf", "/news", "news", "\ud83d\udcf0"));
        return links.stream().limit(4).collect(Collectors.toList());
    }

    private static RelatedLinkVO buildLink(String title, String path, String type, String icon) {
        RelatedLinkVO vo = new RelatedLinkVO();
        vo.setTitle(title);
        vo.setPath(path);
        vo.setType(type);
        vo.setIcon(icon);
        return vo;
    }

    private static String encodeKeyword(String kw) {
        try {
            return java.net.URLEncoder.encode(kw, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return kw;
        }
    }

    private void fillCategoryNames(List<Article> list) {
        Map<String, String> names = Map.of(
                "policy", "\u6559\u80b2\u653f\u7b56",
                "reform", "\u6559\u5b66\u6539\u9769",
                "research", "\u6559\u7814\u52a8\u6001",
                "teacher", "\u540d\u5e08\u8bb2\u5802",
                "exam", "\u5347\u5b66\u5907\u8003"
        );
        for (Article a : list) {
            if (!StringUtils.hasText(a.getCategoryName()) && StringUtils.hasText(a.getCategory())) {
                a.setCategoryName(names.getOrDefault(a.getCategory(), "\u6559\u80b2\u8d44\u8baf"));
            }
        }
    }
}