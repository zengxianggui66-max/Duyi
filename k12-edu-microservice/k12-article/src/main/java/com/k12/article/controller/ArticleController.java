package com.k12.article.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.dto.ArticleCreateDTO;
import com.k12.common.dto.ArticleDetailVO;
import com.k12.common.dto.ArticleQueryDTO;
import com.k12.common.dto.ConsultLeadDTO;
import com.k12.common.dto.NewsHomeVO;
import com.k12.common.entity.Article;
import com.k12.article.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/news")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/home")
    public Result<NewsHomeVO> home() {
        return Result.success(articleService.getHome());
    }

    @GetMapping("/list")
    public Result<PageResult<Article>> listArticles(ArticleQueryDTO query) {
        return Result.success(articleService.listArticles(query));
    }

    @GetMapping("/search")
    public Result<PageResult<Article>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(articleService.search(keyword, current, size));
    }

    @GetMapping("/hot-keywords")
    public Result<List<String>> hotKeywords() {
        return Result.success(List.of(
                "成都中考", "绵阳中考", "招生政策", "期中复习", "高考冲刺",
                "名师讲堂", "新高考选科", "暑假作业", "教研动态", "升学规划"
        ));
    }

    @PostMapping
    public Result<Long> createArticle(
            @Valid @RequestBody ArticleCreateDTO dto,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-Username", required = false) String username) {
        return Result.success(articleService.createArticle(dto, userId, username));
    }

    @GetMapping("/detail/{id}")
    public Result<ArticleDetailVO> getDetail(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(articleService.getDetail(id, userId));
    }

    @PostMapping("/{id}/collect")
    public Result<Void> collect(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        articleService.collect(userId, id);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/collect")
    public Result<Void> uncollect(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId) {
        articleService.uncollect(userId, id);
        return Result.success(null);
    }

    @GetMapping("/{id}/collected")
    public Result<Map<String, Boolean>> isCollected(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return Result.success(Map.of("collected", articleService.isCollected(userId, id)));
    }

    @PostMapping("/consult/lead")
    public Result<Void> submitConsult(
            @Valid @RequestBody ConsultLeadDTO dto,
            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        articleService.submitConsultLead(dto, userId);
        return Result.success("提交成功，顾问将尽快联系您", null);
    }
}
