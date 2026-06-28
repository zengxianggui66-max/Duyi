package com.k12.resource.controller;



import com.k12.common.Result;

import com.k12.common.dto.SearchAllResultVO;

import com.k12.common.dto.SearchClickRequest;

import com.k12.common.dto.SearchRedirectVO;

import com.k12.common.dto.SearchSuggestItemVO;

import com.k12.resource.security.UserIdResolver;

import com.k12.resource.service.SearchService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;



import java.util.List;

import java.util.Map;



/**

 * 搜索和排序控制器

 */

@RestController

@RequestMapping("/api/search")
public class SearchController {



    private final SearchService searchService;

    private final UserIdResolver userIdResolver;
    public SearchController(SearchService searchService, UserIdResolver userIdResolver) {
        this.searchService = searchService;
        this.userIdResolver = userIdResolver;
    }




    @GetMapping("/hot-keywords")

    public Result<List<Map<String, Object>>> getHotKeywords(

            @RequestParam(defaultValue = "10") Integer limit) {

        return Result.success(searchService.getHotKeywords(limit));

    }



    @PostMapping("/keyword")

    public Result<Void> recordKeyword(

            @RequestParam String keyword,

            @RequestParam(required = false) Long userId,

            HttpServletRequest request) {

        Long resolvedUserId = userId != null ? userId : userIdResolver.resolve(request);

        searchService.recordSearchKeyword(keyword, resolvedUserId);

        return Result.success();

    }



    @GetMapping("/history")

    public Result<List<String>> getSearchHistory(

            @RequestParam Long userId,

            @RequestParam(defaultValue = "20") Integer limit) {

        return Result.success(searchService.getSearchHistory(userId, limit));

    }



    @DeleteMapping("/history")

    public Result<Void> clearHistory(@RequestParam Long userId) {

        searchService.clearSearchHistory(userId);

        return Result.success();

    }



    @GetMapping("/all")

    public Result<SearchAllResultVO> searchAll(

            @RequestParam String q,

            @RequestParam(defaultValue = "1") Integer page,

            @RequestParam(defaultValue = "20") Integer size,

            @RequestParam(required = false) String stage,

            @RequestParam(required = false) String channel,

            @RequestParam(required = false) String type,

            @RequestParam(required = false) String domain,

            @RequestParam(defaultValue = "score") String sort,

            @RequestParam(required = false) String searchEngine,

            @RequestParam(required = false) Long userId,

            HttpServletRequest request) {

        Long resolvedUserId = userId != null ? userId : userIdResolver.resolve(request);

        return Result.success(searchService.searchAll(

                q, page, size, stage, channel, type, domain, sort, resolvedUserId, resolveClientKey(request), searchEngine));

    }



    @GetMapping("/suggest")

    public Result<List<SearchSuggestItemVO>> suggest(

            @RequestParam String q,

            @RequestParam(defaultValue = "10") Integer limit,

            @RequestParam(required = false) Long userId,

            HttpServletRequest request) {

        Long resolvedUserId = userId != null ? userId : userIdResolver.resolve(request);

        return Result.success(searchService.suggest(q, limit, resolvedUserId));

    }



    @GetMapping("/redirect")

    public Result<SearchRedirectVO> redirect(@RequestParam String q) {

        return Result.success(searchService.redirect(q));

    }



    @PostMapping("/click")

    public Result<Void> recordClick(

            @RequestBody SearchClickRequest request,

            @RequestParam(required = false) Long userId,

            HttpServletRequest httpRequest) {

        Long resolvedUserId = userId != null ? userId : userIdResolver.resolve(httpRequest);

        searchService.recordClick(request, resolvedUserId);

        return Result.success();

    }



    private String resolveClientKey(HttpServletRequest request) {

        if (request == null) {

            return "anonymous";

        }

        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {

            return "ip:" + forwarded.split(",")[0].trim();

        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {

            return "ip:" + realIp.trim();

        }

        return "ip:" + request.getRemoteAddr();

    }

}


