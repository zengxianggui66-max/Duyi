package com.k12.common.dto;

import com.k12.common.entity.Article;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NewsHomeVO {
    private List<Article> headlines;
    private Map<String, List<Article>> channels;
    private List<String> hotKeywords;

    public List<Article> getHeadlines() {
        return headlines;
    }

    public void setHeadlines(List<Article> headlines) {
        this.headlines = headlines;
    }

    public Map<String, List<Article>> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, List<Article>> channels) {
        this.channels = channels;
    }

    public List<String> getHotKeywords() {
        return hotKeywords;
    }

    public void setHotKeywords(List<String> hotKeywords) {
        this.hotKeywords = hotKeywords;
    }
}
