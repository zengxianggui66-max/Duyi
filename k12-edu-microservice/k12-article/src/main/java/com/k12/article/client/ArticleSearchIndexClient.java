package com.k12.article.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ArticleSearchIndexClient {

    private final RestTemplate restTemplate;

    @Value("${k12.resource.base-url:http://localhost:8082}")
    private String resourceBaseUrl;

    public ArticleSearchIndexClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void syncArticleAsync(Long articleId) {
        if (articleId == null) {
            return;
        }
        try {
            restTemplate.postForEntity(
                    resourceBaseUrl + "/api/internal/search/articles/" + articleId,
                    null,
                    Void.class);
        } catch (Exception e) {
            log.warn("article search index sync failed, id={}: {}", articleId, e.getMessage());
        }
    }
}
