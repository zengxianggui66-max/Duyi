package com.k12.article.service;

import com.k12.common.PageResult;
import com.k12.common.dto.AdminArticleQueryDTO;
import com.k12.common.dto.ArticleCreateDTO;
import com.k12.common.dto.ArticleDetailVO;
import com.k12.common.dto.ArticleQueryDTO;
import com.k12.common.dto.ConsultLeadDTO;
import com.k12.common.dto.NewsHomeVO;
import com.k12.common.entity.Article;

public interface ArticleService {

    PageResult<Article> listArticles(ArticleQueryDTO query);

    ArticleDetailVO getDetail(Long id, Long userId);

    NewsHomeVO getHome();

    PageResult<Article> search(String keyword, Integer current, Integer size);

    void collect(Long userId, Long articleId);

    void uncollect(Long userId, Long articleId);

    boolean isCollected(Long userId, Long articleId);

    void submitConsultLead(ConsultLeadDTO dto, Long userId);

    Long createArticle(ArticleCreateDTO dto, Long userId, String username);

    PageResult<Article> listAdminArticles(AdminArticleQueryDTO query);

    Article getAdminArticle(Long id);

    void updateArticle(Long id, ArticleCreateDTO dto);

    void updateArticleStatus(Long id, int status);

    void deleteArticle(Long id);
}
