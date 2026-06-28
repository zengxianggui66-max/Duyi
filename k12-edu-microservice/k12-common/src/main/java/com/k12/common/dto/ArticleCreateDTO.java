package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleCreateDTO {
    @NotBlank(message = "请输入标题")
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    @NotBlank(message = "请选择分类")
    private String category;
    private String author;
    private String tags;
    private String gradeLevels;
    private String regions;
    private String policyPoints;
    private String relatedTopicKeywords;
    private Integer consultEnabled;
    private String contentType;
    private LocalDateTime publishTime;
    /** 0 草稿 1 发布 */
    private Integer status;
    /** 1 置顶 */
    private Integer isTop;
    private Integer topOrder;
    /** 1 精选 */
    private Integer isFeatured;

    public Integer getConsultEnabled() {
        return consultEnabled;
    }

    public void setConsultEnabled(Integer consultEnabled) {
        this.consultEnabled = consultEnabled;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getGradeLevels() {
        return gradeLevels;
    }

    public void setGradeLevels(String gradeLevels) {
        this.gradeLevels = gradeLevels;
    }

    public String getRegions() {
        return regions;
    }

    public void setRegions(String regions) {
        this.regions = regions;
    }

    public String getPolicyPoints() {
        return policyPoints;
    }

    public void setPolicyPoints(String policyPoints) {
        this.policyPoints = policyPoints;
    }

    public String getRelatedTopicKeywords() {
        return relatedTopicKeywords;
    }

    public void setRelatedTopicKeywords(String relatedTopicKeywords) {
        this.relatedTopicKeywords = relatedTopicKeywords;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getTopOrder() {
        return topOrder;
    }

    public void setTopOrder(Integer topOrder) {
        this.topOrder = topOrder;
    }

    public Integer getIsFeatured() {
        return isFeatured;
    }

    public void setIsFeatured(Integer isFeatured) {
        this.isFeatured = isFeatured;
    }
}
