package com.k12.common.dto;

import com.k12.common.entity.Article;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDetailVO {
    private Article article;
    private List<Article> relatedArticles;
    private List<RelatedLinkVO> relatedLinks;
    private Boolean collected;

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }

    public List<Article> getRelatedArticles() {
        return relatedArticles;
    }

    public void setRelatedArticles(List<Article> relatedArticles) {
        this.relatedArticles = relatedArticles;
    }

    public List<RelatedLinkVO> getRelatedLinks() {
        return relatedLinks;
    }

    public void setRelatedLinks(List<RelatedLinkVO> relatedLinks) {
        this.relatedLinks = relatedLinks;
    }

    public Boolean getCollected() {
        return collected;
    }

    public void setCollected(Boolean collected) {
        this.collected = collected;
    }
}
