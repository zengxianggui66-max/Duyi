package com.k12.prep.controller;

import com.k12.common.Result;
import com.k12.common.dto.BasketDownloadSummaryVO;
import com.k12.common.dto.PrepBasketItemDTO;
import com.k12.common.dto.PrepBasketVO;
import com.k12.common.entity.PrepBasketItem;
import com.k12.prep.service.PrepBasketDownloadService;
import com.k12.prep.service.PrepBasketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prep/basket")
@RequiredArgsConstructor
public class PrepBasketController {

    private final PrepBasketService prepBasketService;
    private final PrepBasketDownloadService prepBasketDownloadService;

    @GetMapping
    public Result<PrepBasketVO> getBasket(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(prepBasketService.getBasket(userId));
    }

    @PostMapping("/items")
    public Result<PrepBasketItem> addItem(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody PrepBasketItemDTO dto) {
        return Result.success(prepBasketService.addItem(userId, dto));
    }

    @DeleteMapping("/items/{itemId}")
    public Result<Void> removeItem(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long itemId) {
        prepBasketService.removeItem(userId, itemId);
        return Result.success(null);
    }

    @DeleteMapping("/clear")
    public Result<Void> clear(@RequestHeader("X-User-Id") Long userId) {
        prepBasketService.clear(userId);
        return Result.success(null);
    }

    @PutMapping("/reorder")
    public Result<Void> reorder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<Long> orderedItemIds) {
        prepBasketService.reorder(userId, orderedItemIds);
        return Result.success(null);
    }

    @PostMapping("/merge")
    public Result<Map<String, Integer>> merge(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody List<PrepBasketItemDTO> items) {
        int merged = prepBasketService.mergeItems(userId, items);
        return Result.success(Map.of("merged", merged));
    }

    @GetMapping("/download-summary")
    public Result<BasketDownloadSummaryVO> downloadSummary(@RequestHeader("X-User-Id") Long userId) {
        return Result.success(prepBasketDownloadService.getDownloadSummary(userId));
    }

    @GetMapping("/download-zip")
    public ResponseEntity<byte[]> downloadZip(@RequestHeader("X-User-Id") Long userId) {
        byte[] body = prepBasketDownloadService.buildBasketZip(userId);
        String filename = URLEncoder.encode("\u8d44\u6599\u7bee\u6279\u91cf\u4e0b\u8f7d.zip", StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(body);
    }

    @GetMapping("/exists")
    public Result<Map<String, Boolean>> exists(
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestParam String itemType,
            @RequestParam Long refId) {
        return Result.success(Map.of("exists", prepBasketService.exists(userId, itemType, refId)));
    }
}
