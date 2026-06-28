package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.annotation.AdminLog;
import com.k12.common.annotation.RequiresPermission;
import com.k12.common.dto.AdminSearchHotKeywordVO;
import com.k12.common.dto.AdminSearchIntentRuleVO;
import com.k12.common.dto.AdminSearchRedirectVO;
import com.k12.common.dto.AdminSearchRedirectWriteDTO;
import com.k12.common.dto.AdminSearchSynonymVO;
import com.k12.common.dto.AdminSearchSynonymWriteDTO;
import com.k12.common.dto.SearchP3ReadinessVO;
import com.k12.common.dto.SearchStatsVO;
import com.k12.resource.service.AdminSearchHotKeywordService;
import com.k12.resource.service.AdminSearchLexiconService;
import com.k12.resource.service.SearchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Phase 9-A/B：搜索运营管理
 */
@RestController
@RequestMapping("/api/admin/search")
public class AdminSearchController {

    private final SearchService searchService;
    private final AdminSearchLexiconService adminSearchLexiconService;
    private final AdminSearchHotKeywordService adminSearchHotKeywordService;
    public AdminSearchController(SearchService searchService, AdminSearchLexiconService adminSearchLexiconService, AdminSearchHotKeywordService adminSearchHotKeywordService) {
        this.searchService = searchService;
        this.adminSearchLexiconService = adminSearchLexiconService;
        this.adminSearchHotKeywordService = adminSearchHotKeywordService;
    }


    @GetMapping("/stats")
    @RequiresPermission("admin:search:view")
    public Result<SearchStatsVO> stats(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(searchService.searchStats(days));
    }

    @GetMapping("/p3-readiness")
    @RequiresPermission("admin:search:view")
    public Result<SearchP3ReadinessVO> p3Readiness(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(searchService.searchP3Readiness(days));
    }

    @GetMapping("/engine/health")
    @RequiresPermission("admin:search:view")
    public Result<Map<String, Object>> engineHealth() {
        return Result.success(searchService.searchEngineHealth());
    }

    @PostMapping("/reindex")
    @RequiresPermission("admin:search:reindex")
    @AdminLog(module = "search", action = "reindex", permission = "admin:search:reindex")
    public Result<Integer> reindex() {
        return Result.success(searchService.rebuildSearchIndex());
    }

    @PostMapping("/engine/sync")
    @RequiresPermission("admin:search:reindex")
    @AdminLog(module = "search", action = "engine_sync", permission = "admin:search:reindex")
    public Result<Integer> engineSync() {
        return Result.success(searchService.syncSearchEngine());
    }

    // ---------- Phase 9-B：同义词 ----------

    @GetMapping("/synonyms")
    @RequiresPermission("admin:search:view")
    public Result<List<AdminSearchSynonymVO>> listSynonyms(
            @RequestParam(defaultValue = "false") boolean includeDisabled,
            @RequestParam(required = false) String domain) {
        return Result.success(adminSearchLexiconService.listSynonyms(includeDisabled, domain));
    }

    @PostMapping("/synonyms")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "create_synonym", permission = "admin:search:edit")
    public Result<AdminSearchSynonymVO> createSynonym(@Valid @RequestBody AdminSearchSynonymWriteDTO dto) {
        return Result.success(adminSearchLexiconService.createSynonym(dto));
    }

    @PostMapping("/synonyms/draft")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "create_synonym_draft", permission = "admin:search:edit")
    public Result<AdminSearchSynonymVO> createSynonymDraft(@RequestParam String keyword) {
        return Result.success(adminSearchLexiconService.createSynonymDraft(keyword));
    }

    @PutMapping("/synonyms/{id}")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "update_synonym", permission = "admin:search:edit")
    public Result<AdminSearchSynonymVO> updateSynonym(
            @PathVariable Long id,
            @Valid @RequestBody AdminSearchSynonymWriteDTO dto) {
        return Result.success(adminSearchLexiconService.updateSynonym(id, dto));
    }

    @PutMapping("/synonyms/{id}/status")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "toggle_synonym", permission = "admin:search:edit")
    public Result<Void> setSynonymStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminSearchLexiconService.setSynonymStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/synonyms/{id}")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "delete_synonym", permission = "admin:search:edit")
    public Result<Void> deleteSynonym(@PathVariable Long id) {
        adminSearchLexiconService.deleteSynonym(id);
        return Result.success();
    }

    // ---------- Phase 9-B：重定向 ----------

    @GetMapping("/redirects")
    @RequiresPermission("admin:search:view")
    public Result<List<AdminSearchRedirectVO>> listRedirects(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(adminSearchLexiconService.listRedirects(includeDisabled));
    }

    @PostMapping("/redirects")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "create_redirect", permission = "admin:search:edit")
    public Result<AdminSearchRedirectVO> createRedirect(@Valid @RequestBody AdminSearchRedirectWriteDTO dto) {
        return Result.success(adminSearchLexiconService.createRedirect(dto));
    }

    @PutMapping("/redirects/{id}")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "update_redirect", permission = "admin:search:edit")
    public Result<AdminSearchRedirectVO> updateRedirect(
            @PathVariable Long id,
            @Valid @RequestBody AdminSearchRedirectWriteDTO dto) {
        return Result.success(adminSearchLexiconService.updateRedirect(id, dto));
    }

    @PutMapping("/redirects/{id}/status")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "toggle_redirect", permission = "admin:search:edit")
    public Result<Void> setRedirectStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminSearchLexiconService.setRedirectStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/redirects/{id}")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "delete_redirect", permission = "admin:search:edit")
    public Result<Void> deleteRedirect(@PathVariable Long id) {
        adminSearchLexiconService.deleteRedirect(id);
        return Result.success();
    }

    // ---------- Phase 9-B：意图规则（只读） ----------

    @GetMapping("/intent-rules")
    @RequiresPermission("admin:search:view")
    public Result<List<AdminSearchIntentRuleVO>> listIntentRules(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(adminSearchLexiconService.listIntentRules(includeDisabled));
    }

    // ---------- Phase 9-C：搜索热词（统计驱动） ----------

    @GetMapping("/hot-keywords")
    @RequiresPermission("admin:search:view")
    public Result<List<AdminSearchHotKeywordVO>> listHotKeywords(
            @RequestParam(defaultValue = "false") boolean includeDisabled) {
        return Result.success(adminSearchHotKeywordService.list(includeDisabled));
    }

    @PutMapping("/hot-keywords/{id}/status")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "toggle_hot_keyword", permission = "admin:search:edit")
    public Result<Void> setHotKeywordStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminSearchHotKeywordService.setStatus(id, status);
        return Result.success();
    }

    @PutMapping("/hot-keywords/{id}/boost")
    @RequiresPermission("admin:search:edit")
    @AdminLog(module = "search", action = "boost_hot_keyword", permission = "admin:search:edit")
    public Result<AdminSearchHotKeywordVO> updateHotKeywordBoost(
            @PathVariable Long id,
            @RequestParam Integer boostScore) {
        return Result.success(adminSearchHotKeywordService.updateBoostScore(id, boostScore));
    }
}
