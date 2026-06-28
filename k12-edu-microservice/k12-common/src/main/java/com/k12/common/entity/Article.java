package com.k12.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 文章/资讯实体
 */
@Data
@TableName("article")
public class Article {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String coverUrl;
    private String category;
    private String categoryName;
    private String author;
    private Integer viewCount;
    private Integer commentCount;
    private Integer likeCount;
    private String tags;
    private String subCategory;
    private String gradeLevels;
    private String regions;
    private String sourceName;
    private String sourceUrl;
    private Integer isTop;
    private Integer topOrder;
    private Integer isFeatured;
    private LocalDateTime publishTime;
    /** JSON 数组字符串，政策类文章要点 */
    private String policyPoints;
    private String relatedTopicKeywords;
    private Integer consultEnabled;
    private String contentType;
    private Integer status;
    @TableLogic
    private Integer deleted;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
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

    public LocalDateTime getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(LocalDateTime publishTime) {
        this.publishTime = publishTime;
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

    public Integer getConsultEnabled() {
        return consultEnabled;
    }

    public void setConsultEnabled(Integer consultEnabled) {
        this.consultEnabled = consultEnabled;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
