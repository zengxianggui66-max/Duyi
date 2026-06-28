package com.k12.prep.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.BasketDownloadSummaryVO;
import com.k12.common.dto.PrepBasketVO;
import com.k12.common.entity.PrepBasketItem;
import com.k12.common.entity.Resource;
import com.k12.common.entity.TopicResource;
import com.k12.prep.mapper.ResourceMapper;
import com.k12.prep.mapper.TopicResourceMapper;
import com.k12.prep.service.PrepBasketDownloadService;
import com.k12.prep.service.PrepBasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class PrepBasketDownloadServiceImpl implements PrepBasketDownloadService {

    private static final int MAX_FILES = 50;
    private static final Set<String> DOWNLOAD_TYPES = Set.of("resource", "paper");

    private final PrepBasketService prepBasketService;
    private final TopicResourceMapper topicResourceMapper;
    private final ResourceMapper resourceMapper;
    private final ObjectMapper objectMapper;

    @Value("${prep.download.file-base-url:http://localhost:8082/uploads}")
    private String fileBaseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(15))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    @Override
    public BasketDownloadSummaryVO getDownloadSummary(Long userId) {
        PrepBasketVO basket = prepBasketService.getBasket(userId);
        BasketDownloadSummaryVO vo = new BasketDownloadSummaryVO();
        vo.setTotalItems(basket.getItems().size());
        List<String> skipped = new ArrayList<>();
        int downloadable = 0;
        for (PrepBasketItem item : basket.getItems()) {
            if (!DOWNLOAD_TYPES.contains(item.getItemType())) {
                skipped.add(item.getTitle() + "（" + item.getItemType() + " 类型不支持打包）");
                continue;
            }
            if (resolveFileUrl(item) != null) {
                downloadable++;
            } else {
                skipped.add(item.getTitle() + "（暂无文件链接）");
            }
        }
        vo.setDownloadableCount(downloadable);
        vo.setSkippedCount(skipped.size());
        vo.setSkippedReasons(skipped.size() > 20 ? skipped.subList(0, 20) : skipped);
        return vo;
    }

    @Override
    public byte[] buildBasketZip(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "\u8bf7\u5148\u767b\u5f55");
        }
        PrepBasketVO basket = prepBasketService.getBasket(userId);
        List<PrepBasketItem> candidates = basket.getItems().stream()
                .filter(i -> DOWNLOAD_TYPES.contains(i.getItemType()))
                .toList();
        if (candidates.isEmpty()) {
            throw new BusinessException("\u8d44\u6599\u7bee\u4e2d\u6ca1\u6709\u53ef\u4e0b\u8f7d\u7684\u8d44\u6e90\u6216\u8bd5\u5377");
        }

        List<String> manifest = new ArrayList<>();
        manifest.add("\u8d44\u6599\u7bee\u6279\u91cf\u4e0b\u8f7d\u6e05\u5355");
        manifest.add("\u7bee\u540d\uff1a" + basket.getName());
        manifest.add("");

        int success = 0;
        Set<String> usedNames = new LinkedHashSet<>();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            for (PrepBasketItem item : candidates) {
                if (success >= MAX_FILES) {
                    manifest.add("\u2026\u8d85\u8fc7\u5355\u6b21\u4e0a\u9650 " + MAX_FILES + " \u4e2a\u6587\u4ef6\uff0c\u5df2\u622a\u65ad");
                    break;
                }
                String fileUrl = resolveFileUrl(item);
                if (!StringUtils.hasText(fileUrl)) {
                    manifest.add("[\u8df3\u8fc7] " + item.getTitle() + " - \u6682\u65e0\u6587\u4ef6");
                    continue;
                }
                String ext = guessExtension(fileUrl, item);
                String entryName = uniqueName(usedNames, sanitizeFileName(item.getTitle()) + ext);
                try {
                    byte[] data = fetchFileBytes(fileUrl);
                    if (data == null || data.length == 0) {
                        manifest.add("[\u5931\u8d25] " + item.getTitle() + " - \u6587\u4ef6\u4e3a\u7a7a");
                        continue;
                    }
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    zos.write(data);
                    zos.closeEntry();
                    success++;
                    manifest.add("[\u6210\u529f] " + item.getTitle() + " -> " + entryName);
                } catch (Exception e) {
                    manifest.add("[\u5931\u8d25] " + item.getTitle() + " - " + e.getMessage());
                }
            }

            manifest.add("");
            manifest.add("\u5171\u8ba1\u6210\u529f " + success + " \u4e2a\u6587\u4ef6");
            addTextEntry(zos, "\u4e0b\u8f7d\u8bf4\u660e.txt", String.join("\r\n", manifest));

            zos.finish();
            if (success == 0) {
                throw new BusinessException("\u6ca1\u6709\u53ef\u6210\u529f\u4e0b\u8f7d\u7684\u6587\u4ef6\uff0c\u8bf7\u786e\u8ba4\u8d44\u6e90\u5df2\u4e0a\u4f20\u6587\u4ef6");
            }
            return baos.toByteArray();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("\u6253\u5305\u4e0b\u8f7d\u5931\u8d25");
        }
    }

    private String resolveFileUrl(PrepBasketItem item) {
        String fromMeta = parseMetaFileUrl(item.getMetaJson());
        if (StringUtils.hasText(fromMeta)) {
            return normalizeUrl(fromMeta);
        }
        if ("resource".equals(item.getItemType()) || "paper".equals(item.getItemType())) {
            TopicResource topic = topicResourceMapper.selectById(item.getRefId());
            if (topic != null && StringUtils.hasText(topic.getFileUrl())) {
                return normalizeUrl(topic.getFileUrl());
            }
            Resource resource = resourceMapper.selectById(item.getRefId());
            if (resource != null && StringUtils.hasText(resource.getFileUrl())) {
                return normalizeUrl(resource.getFileUrl());
            }
        }
        return null;
    }

    private String parseMetaFileUrl(String metaJson) {
        if (!StringUtils.hasText(metaJson)) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(metaJson);
            if (node.hasNonNull("fileUrl")) {
                return node.get("fileUrl").asText();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String base = fileBaseUrl.endsWith("/") ? fileBaseUrl.substring(0, fileBaseUrl.length() - 1) : fileBaseUrl;
        String path = trimmed.startsWith("/") ? trimmed : "/" + trimmed;
        return base + path;
    }

    private byte[] fetchFileBytes(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .GET()
                .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() >= 400) {
            throw new BusinessException("HTTP " + response.statusCode());
        }
        try (InputStream in = response.body()) {
            return in.readAllBytes();
        }
    }

    private static void addTextEntry(ZipOutputStream zos, String name, String text) throws Exception {
        ZipEntry entry = new ZipEntry(name);
        zos.putNextEntry(entry);
        zos.write(text.getBytes(StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String sanitizeFileName(String title) {
        if (!StringUtils.hasText(title)) {
            return "resource";
        }
        String sanitized = title.replaceAll("[\\\\/:*?\"<>|]", "_").trim();
        return sanitized.substring(0, Math.min(80, sanitized.length()));
    }

    private static String uniqueName(Set<String> used, String base) {
        String name = base;
        int i = 1;
        while (used.contains(name)) {
            int dot = base.lastIndexOf('.');
            if (dot > 0) {
                name = base.substring(0, dot) + "_" + i + base.substring(dot);
            } else {
                name = base + "_" + i;
            }
            i++;
        }
        used.add(name);
        return name;
    }

    private static String guessExtension(String fileUrl, PrepBasketItem item) {
        String lower = fileUrl.toLowerCase();
        if (lower.contains(".pdf")) return ".pdf";
        if (lower.contains(".docx")) return ".docx";
        if (lower.contains(".doc")) return ".doc";
        if (lower.contains(".pptx")) return ".pptx";
        if (lower.contains(".ppt")) return ".ppt";
        if (lower.contains(".zip")) return ".zip";
        try {
            if (StringUtils.hasText(item.getMetaJson())) {
                JsonNode n = new ObjectMapper().readTree(item.getMetaJson());
                if (n.hasNonNull("fileFormat")) {
                    String fmt = n.get("fileFormat").asText().toLowerCase();
                    if (!fmt.startsWith(".")) {
                        return "." + fmt;
                    }
                    return fmt;
                }
            }
        } catch (Exception ignored) {
        }
        return ".bin";
    }
}
