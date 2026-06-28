package com.k12.resource.service.impl;

import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.resource.config.PreviewProperties;
import com.k12.resource.preview.LibreOfficeConverter;
import com.k12.resource.preview.PoiHtmlConverter;
import com.k12.resource.preview.PptSlideImageExporter;
import com.k12.resource.preview.PreviewJobManager;
import com.k12.resource.service.DocumentPreviewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Slf4j
@Service
@SuppressWarnings("null")
public class DocumentPreviewServiceImpl implements DocumentPreviewService {

    private static final Set<String> OFFICE_EXTENSIONS = Set.of(
            "doc", "docx", "ppt", "pptx", "xls", "xlsx"
    );

    private static final Set<String> PPT_EXTENSIONS = Set.of("ppt", "pptx");

    private static final Set<String> DOC_EXTENSIONS = Set.of("doc", "docx");

    private final PreviewProperties previewProperties;
    private final LibreOfficeConverter libreOfficeConverter;
    private final PoiHtmlConverter poiHtmlConverter;
    private final PptSlideImageExporter pptSlideImageExporter;
    private final PreviewJobManager previewJobManager;
    public DocumentPreviewServiceImpl(PreviewProperties previewProperties, LibreOfficeConverter libreOfficeConverter, PoiHtmlConverter poiHtmlConverter, PptSlideImageExporter pptSlideImageExporter, PreviewJobManager previewJobManager) {
        this.previewProperties = previewProperties;
        this.libreOfficeConverter = libreOfficeConverter;
        this.poiHtmlConverter = poiHtmlConverter;
        this.pptSlideImageExporter = pptSlideImageExporter;
        this.previewJobManager = previewJobManager;
    }


    @Value("${upload.path:${user.home}/k12-uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Override
    public FilePreviewInfoDTO resolvePreview(String fileUrl) {
        String originalUrl = toAccessibleUrl(fileUrl);
        if (fileUrl == null || fileUrl.isBlank()) {
            return FilePreviewInfoDTO.builder()
                    .originalUrl(originalUrl)
                    .previewMode("embed")
                    .provider("none")
                    .message("文件地址为空")
                    .build();
        }

        String ext = getExtensionFromUrl(fileUrl).toLowerCase(Locale.ROOT);
        if (previewProperties.isEnabled() && previewProperties.isAsyncEnabled()) {
            if (PPT_EXTENSIONS.contains(ext)) {
                FilePreviewInfoDTO cached = tryPptCacheOnly(fileUrl, originalUrl);
                if (cached != null) {
                    return cached;
                }
                return resolveAsyncPreview(fileUrl, originalUrl, ext);
            }
            if (DOC_EXTENSIONS.contains(ext)) {
                FilePreviewInfoDTO cached = tryWordPdfCacheOnly(fileUrl, originalUrl);
                if (cached != null) {
                    return cached;
                }
                return resolveAsyncPreview(fileUrl, originalUrl, ext);
            }
        }

        return resolvePreviewSync(fileUrl);
    }

    private FilePreviewInfoDTO resolveAsyncPreview(String fileUrl, String originalUrl, String ext) {
        String jobKey = buildCacheKeyFromUrl(fileUrl);
        previewJobManager.evictFailedIfStale(jobKey, 60_000L);
        FilePreviewInfoDTO ready = previewJobManager.getReadyResult(jobKey);
        if (ready != null) {
            return ready;
        }
        if (previewJobManager.getStatus(jobKey) == PreviewJobManager.Status.FAILED) {
            FilePreviewInfoDTO failed = previewJobManager.getFailedResult(jobKey, originalUrl);
            if (failed != null) {
                return failed;
            }
        }
        if (previewJobManager.getStatus(jobKey) == PreviewJobManager.Status.PROCESSING) {
            return previewJobManager.processingInfo(originalUrl,
                    DOC_EXTENSIONS.contains(ext)
                            ? "正在将 Word 转为 PDF，请稍候…（首次预览约需 1–2 分钟）"
                            : "正在转码幻灯片，请稍候…（首次预览约需 1–2 分钟）");
        }
        previewJobManager.startIfAbsent(jobKey, originalUrl, () -> resolvePreviewSync(fileUrl));
        return previewJobManager.processingInfo(originalUrl,
                DOC_EXTENSIONS.contains(ext)
                        ? "正在将 Word 转为 PDF，请稍候…（首次预览约需 1–2 分钟）"
                        : "正在转码幻灯片，请稍候…（首次预览约需 1–2 分钟）");
    }

    /** 同步解析（原逻辑，含下载与 LibreOffice 转码） */
    private FilePreviewInfoDTO resolvePreviewSync(String fileUrl) {
        String originalUrl = toAccessibleUrl(fileUrl);
        if (fileUrl == null || fileUrl.isBlank()) {
            return FilePreviewInfoDTO.builder()
                    .originalUrl(originalUrl)
                    .previewMode("embed")
                    .provider("none")
                    .message("文件地址为空")
                    .build();
        }

        File localFile = resolveLocalFile(fileUrl);
        if (localFile == null || !localFile.exists()) {
            localFile = downloadRemoteFileForPreview(fileUrl);
        }
        if (localFile == null || !localFile.exists()) {
            String ext = getExtensionFromUrl(fileUrl);
            if (isMediaOrImage(ext)) {
                return nativeInfo(originalUrl, mapPreviewType(ext),
                        "远程媒体文件，建议通过服务端代理地址播放");
            }
            return FilePreviewInfoDTO.builder()
                    .previewUrl(originalUrl)
                    .originalUrl(originalUrl)
                    .previewMode("embed")
                    .provider("none")
                    .converted(false)
                    .message("无法定位本地文件，将尝试在线嵌入预览")
                    .build();
        }

        String ext = getExtension(localFile.getName()).toLowerCase(Locale.ROOT);
        String previewType = mapPreviewType(ext);

        if (!previewProperties.isEnabled()) {
            return nativeInfo(originalUrl, previewType, "预览服务已关闭");
        }

        if ("pdf".equals(ext)) {
            return nativeInfo(originalUrl, "document", null);
        }
        if (isMediaOrImage(ext)) {
            return nativeInfo(originalUrl, previewType, null);
        }

        if (!OFFICE_EXTENSIONS.contains(ext)) {
            return FilePreviewInfoDTO.builder()
                    .previewUrl(originalUrl)
                    .previewType(previewType)
                    .previewMode("native")
                    .originalUrl(originalUrl)
                    .converted(false)
                    .provider("native")
                    .build();
        }

        String cacheKey = buildCacheKey(localFile);
        File cacheDir = new File(uploadPath, "preview-cache");
        File cachedPdf = new File(cacheDir, cacheKey + ".pdf");
        File cachedHtml = new File(cacheDir, cacheKey + ".html");
        File slidesDir = new File(cacheDir, cacheKey + "_slides");

        if (PPT_EXTENSIONS.contains(ext)) {
            return resolvePptPreview(
                    localFile, originalUrl, cacheKey, cacheDir, slidesDir, cachedPdf);
        }

        if (DOC_EXTENSIONS.contains(ext)) {
            return resolveWordDocumentPreview(
                    localFile, originalUrl, cacheKey, cacheDir, cachedPdf, cachedHtml);
        }

        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            return pdfInfo(toAccessibleUrl("preview-cache/" + cachedPdf.getName()), originalUrl);
        }
        if (cachedHtml.exists() && cachedHtml.length() > 0) {
            return htmlInfo(toAccessibleUrl("preview-cache/" + cachedHtml.getName()), originalUrl);
        }

        if (previewProperties.isLibreofficeEnabled() && libreOfficeConverter.isAvailable()) {
            File tempOut = new File(cacheDir, "tmp-" + cacheKey);
            if (!tempOut.exists()) {
                tempOut.mkdirs();
            }
            File pdf = libreOfficeConverter.convertToPdf(localFile, tempOut);
            if (pdf != null && pdf.exists()) {
                try {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    Files.move(pdf.toPath(), cachedPdf.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    deleteDirectory(tempOut);
                    log.info("LibreOffice 转 PDF 成功: {}", localFile.getName());
                    return pdfInfo(toAccessibleUrl("preview-cache/" + cachedPdf.getName()), originalUrl);
                } catch (Exception e) {
                    log.warn("缓存 PDF 失败: {}", e.getMessage());
                    return pdfInfo(toAccessibleUrl(relativePathFromUpload(pdf)), originalUrl);
                }
            }
            deleteDirectory(tempOut);
        }

        if (previewProperties.isPoiFallbackEnabled()) {
            String html = poiHtmlConverter.convertToHtml(localFile, ext);
            if (html != null && !html.isBlank()) {
                try {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    Files.writeString(cachedHtml.toPath(), html, StandardCharsets.UTF_8);
                    log.info("POI HTML 预览已生成: {}", localFile.getName());
                    return htmlInfo(toAccessibleUrl("preview-cache/" + cachedHtml.getName()), originalUrl);
                } catch (Exception e) {
                    log.warn("写入 HTML 缓存失败: {}", e.getMessage());
                }
            }
        }

        return FilePreviewInfoDTO.builder()
                .previewUrl(originalUrl)
                .previewType("document")
                .previewMode("embed")
                .originalUrl(originalUrl)
                .converted(false)
                .provider("none")
                .message("未安装 LibreOffice 或文档无法解析，将尝试在线文档预览；亦可下载原文件查看")
                .build();
    }

  private FilePreviewInfoDTO resolvePptPreview(
            File localFile,
            String originalUrl,
            String cacheKey,
            File cacheDir,
            File slidesDir,
            File cachedPdf) {
        List<String> slideUrls = listCachedSlideUrls(slidesDir);
        if (!slideUrls.isEmpty()) {
            return slidesInfo(slideUrls, originalUrl);
        }

        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            return pdfInfo(
                    toAccessibleUrl("preview-cache/" + cachedPdf.getName()),
                    originalUrl,
                    "已转码为 PDF，可按页浏览幻灯片");
        }

        if (previewProperties.isPptSlidesEnabled()
                && previewProperties.isLibreofficeEnabled()
                && libreOfficeConverter.isAvailable()) {
            if (!slidesDir.exists()) {
                slidesDir.mkdirs();
            }
            File tempSlides = new File(cacheDir, "tmp-slides-" + cacheKey);
            if (!tempSlides.exists()) {
                tempSlides.mkdirs();
            }
            ensurePptPdfCache(localFile, cacheDir, cacheKey, cachedPdf);
            List<File> exported = pptSlideImageExporter.exportSlideImages(
                    localFile,
                    tempSlides,
                    cachedPdf.exists() ? cachedPdf : null);
            if (!exported.isEmpty()) {
                slideUrls = persistSlideImages(slidesDir, exported);
                deleteDirectory(tempSlides);
                if (!slideUrls.isEmpty()) {
                    log.info("PPT 幻灯片图导出成功（PPT→PDF→PNG）: {} 页", slideUrls.size());
                    return slidesInfo(slideUrls, originalUrl);
                }
            }
            deleteDirectory(tempSlides);
        }

        if (previewProperties.isLibreofficeEnabled() && libreOfficeConverter.isAvailable()) {
            String pdfUrl = ensurePptPdfCache(localFile, cacheDir, cacheKey, cachedPdf);
            if (pdfUrl != null) {
                return pdfInfo(pdfUrl, originalUrl, "已转码为 PDF，可按页浏览幻灯片");
            }
        }

        if (previewProperties.isPptPoiFallbackEnabled()) {
            File cachedHtml = new File(cacheDir, cacheKey + ".html");
            String html = poiHtmlConverter.convertToHtml(localFile, getExtension(localFile.getName()));
            if (html != null && !html.isBlank()) {
                try {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    Files.writeString(cachedHtml.toPath(), html, StandardCharsets.UTF_8);
                    return htmlInfo(
                            toAccessibleUrl("preview-cache/" + cachedHtml.getName()),
                            originalUrl,
                            "已生成 HTML 幻灯片摘要（非真实幻灯片渲染）");
                } catch (Exception e) {
                    log.warn("PPT HTML 缓存失败: {}", e.getMessage());
                }
            }
        }

        if (!libreOfficeConverter.isAvailable()) {
            return pptPreviewFailed(originalUrl,
                    "未检测到 LibreOffice，无法转码 PPT 预览，请安装后配置 upload.preview.libreoffice-path");
        }
        return pptPreviewFailed(originalUrl,
                "PPT 转码失败，暂无法在线预览，请下载原文件查看");
    }

    /**
     * Word(doc/docx) 仅走 LibreOffice→PDF，禁止 HTML 缓存与 embed 兜底。
     */
    private FilePreviewInfoDTO resolveWordDocumentPreview(
            File localFile,
            String originalUrl,
            String cacheKey,
            File cacheDir,
            File cachedPdf,
            File cachedHtml) {
        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            deleteStaleHtmlCache(cachedHtml);
            return pdfInfo(
                    toAccessibleUrl("preview-cache/" + cachedPdf.getName()),
                    originalUrl,
                    "已转码为 PDF，可按页浏览");
        }

        deleteStaleHtmlCache(cachedHtml);

        if (previewProperties.isLibreofficeEnabled() && libreOfficeConverter.isAvailable()) {
            File tempOut = new File(cacheDir, "tmp-" + cacheKey);
            if (!tempOut.exists()) {
                tempOut.mkdirs();
            }
            File pdf = libreOfficeConverter.convertToPdf(localFile, tempOut);
            if (pdf != null && pdf.exists()) {
                try {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    Files.move(pdf.toPath(), cachedPdf.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    deleteDirectory(tempOut);
                    deleteStaleHtmlCache(cachedHtml);
                    log.info("Word LibreOffice 转 PDF 成功: {}", localFile.getName());
                    return pdfInfo(
                            toAccessibleUrl("preview-cache/" + cachedPdf.getName()),
                            originalUrl,
                            "已转码为 PDF，可按页浏览");
                } catch (Exception e) {
                    log.warn("Word PDF 缓存失败: {}", e.getMessage());
                    return pdfInfo(toAccessibleUrl(relativePathFromUpload(pdf)), originalUrl,
                            "已转码为 PDF，可按页浏览");
                }
            }
            deleteDirectory(tempOut);
        }

        if (previewProperties.isDocPoiFallbackEnabled()) {
            String html = poiHtmlConverter.convertToHtml(localFile, getExtension(localFile.getName()));
            if (html != null && !html.isBlank()) {
                try {
                    if (!cacheDir.exists()) {
                        cacheDir.mkdirs();
                    }
                    Files.writeString(cachedHtml.toPath(), html, StandardCharsets.UTF_8);
                    log.info("Word POI HTML 预览已生成（兜底）: {}", localFile.getName());
                    return htmlInfo(
                            toAccessibleUrl("preview-cache/" + cachedHtml.getName()),
                            originalUrl,
                            "已生成 HTML 摘要（非 PDF 预览）");
                } catch (Exception e) {
                    log.warn("Word HTML 缓存失败: {}", e.getMessage());
                }
            }
        }

        if (!libreOfficeConverter.isAvailable()) {
            return wordPreviewFailed(originalUrl,
                    "未检测到 LibreOffice，无法将 Word 转为 PDF，请安装后配置 upload.preview.libreoffice-path");
        }
        return wordPreviewFailed(originalUrl,
                "Word 转 PDF 失败，暂无法在线预览，请下载原文件查看");
    }

    private FilePreviewInfoDTO wordPreviewFailed(String originalUrl, String message) {
        return FilePreviewInfoDTO.builder()
                .originalUrl(originalUrl)
                .previewType("document")
                .previewMode("none")
                .converted(false)
                .provider("libreoffice")
                .message(message)
                .build();
    }

    private void deleteStaleHtmlCache(File cachedHtml) {
        if (cachedHtml == null || !cachedHtml.exists()) {
            return;
        }
        try {
            Files.deleteIfExists(cachedHtml.toPath());
            log.info("已清除过时的 HTML 预览缓存: {}", cachedHtml.getName());
        } catch (Exception e) {
            log.warn("清除 HTML 缓存失败: {}", e.getMessage());
        }
    }

    private FilePreviewInfoDTO tryWordPdfCacheOnly(String fileUrl, String originalUrl) {
        File localFile = resolveLocalFile(fileUrl);
        if (localFile == null || !localFile.exists()) {
            localFile = findRemoteCachedFile(fileUrl);
        }
        if (localFile == null || !localFile.exists()) {
            return null;
        }

        String cacheKey = buildCacheKey(localFile);
        File cacheDir = new File(uploadPath, "preview-cache");
        File cachedPdf = new File(cacheDir, cacheKey + ".pdf");
        File cachedHtml = new File(cacheDir, cacheKey + ".html");

        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            deleteStaleHtmlCache(cachedHtml);
            return pdfInfo(
                    toAccessibleUrl("preview-cache/" + cachedPdf.getName()),
                    originalUrl,
                    "已转码为 PDF，可按页浏览");
        }
        return null;
    }

    private FilePreviewInfoDTO pptPreviewFailed(String originalUrl, String message) {
        return FilePreviewInfoDTO.builder()
                .originalUrl(originalUrl)
                .previewType("document")
                .previewMode("none")
                .converted(false)
                .provider("libreoffice")
                .message(message)
                .build();
    }

    /**
     * 仅读磁盘缓存，不触发下载/转码（供异步轮询快速命中）。
     */
    private FilePreviewInfoDTO tryPptCacheOnly(String fileUrl, String originalUrl) {
        File localFile = resolveLocalFile(fileUrl);
        if (localFile == null || !localFile.exists()) {
            localFile = findRemoteCachedFile(fileUrl);
        }
        if (localFile == null || !localFile.exists()) {
            return null;
        }

        String cacheKey = buildCacheKey(localFile);
        File cacheDir = new File(uploadPath, "preview-cache");
        File cachedPdf = new File(cacheDir, cacheKey + ".pdf");
        File slidesDir = new File(cacheDir, cacheKey + "_slides");

        List<String> slideUrls = listCachedSlideUrls(slidesDir);
        if (!slideUrls.isEmpty()) {
            return slidesInfo(slideUrls, originalUrl);
        }
        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            return pdfInfo(
                    toAccessibleUrl("preview-cache/" + cachedPdf.getName()),
                    originalUrl,
                    "已转码为 PDF，可按页浏览幻灯片");
        }
        return null;
    }

    /** 远程 OSS 文件若已下载到 preview-cache/remote 则返回，不发起网络请求 */
    private File findRemoteCachedFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            return null;
        }
        String ext = getExtensionFromUrl(fileUrl);
        if (ext.isBlank()) {
            ext = "bin";
        }
        String cacheKey = buildCacheKeyFromUrl(fileUrl);
        File cacheDir = new File(uploadPath, "preview-cache/remote");
        File cached = new File(cacheDir, cacheKey + "." + ext);
        if (cached.exists() && cached.length() > 0) {
            return cached;
        }
        return null;
    }

    private String ensurePptPdfCache(File localFile, File cacheDir, String cacheKey, File cachedPdf) {
        if (cachedPdf.exists() && cachedPdf.length() > 0) {
            return toAccessibleUrl("preview-cache/" + cachedPdf.getName());
        }
        File tempOut = new File(cacheDir, "tmp-pdf-" + cacheKey);
        if (!tempOut.exists()) {
            tempOut.mkdirs();
        }
        File pdf = libreOfficeConverter.convertToPdf(localFile, tempOut);
        if (pdf != null && pdf.exists()) {
            try {
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                Files.move(pdf.toPath(), cachedPdf.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                deleteDirectory(tempOut);
                return toAccessibleUrl("preview-cache/" + cachedPdf.getName());
            } catch (Exception e) {
                log.warn("PPT PDF 缓存失败: {}", e.getMessage());
                return toAccessibleUrl(relativePathFromUpload(pdf));
            }
        }
        deleteDirectory(tempOut);
        return null;
    }

    private List<String> listCachedSlideUrls(File slidesDir) {
        if (slidesDir == null || !slidesDir.exists()) {
            return List.of();
        }
        File[] pngs = slidesDir.listFiles((dir, name) ->
                name.toLowerCase(Locale.ROOT).startsWith("slide-")
                        && name.toLowerCase(Locale.ROOT).endsWith(".png"));
        if (pngs == null || pngs.length == 0) {
            return List.of();
        }
        java.util.Arrays.sort(pngs, java.util.Comparator.comparing(File::getName));
        List<String> urls = new ArrayList<>();
        for (File png : pngs) {
            urls.add(toAccessibleUrl(relativePathFromUpload(png)));
        }
        return urls;
    }

    private List<String> persistSlideImages(File slidesDir, List<File> exported) {
        if (!slidesDir.exists()) {
            slidesDir.mkdirs();
        }
        List<String> urls = new ArrayList<>();
        int index = 1;
        for (File src : exported) {
            File target = new File(slidesDir, String.format(Locale.ROOT, "slide-%03d.png", index++));
            try {
                Files.copy(src.toPath(), target.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                urls.add(toAccessibleUrl(relativePathFromUpload(target)));
            } catch (Exception e) {
                log.warn("保存幻灯片图失败: {}", e.getMessage());
            }
        }
        return urls;
    }

    private FilePreviewInfoDTO slidesInfo(List<String> slideUrls, String originalUrl) {
        return FilePreviewInfoDTO.builder()
                .previewUrl(slideUrls.get(0))
                .previewType("document")
                .previewMode("slides")
                .originalUrl(originalUrl)
                .converted(true)
                .provider("libreoffice")
                .slideUrls(slideUrls)
                .slideCount(slideUrls.size())
                .message("已导出 " + slideUrls.size() + " 张真实幻灯片预览图（PPT→PDF→PNG）")
                .build();
    }

    private FilePreviewInfoDTO nativeInfo(String url, String previewType, String message) {
        return FilePreviewInfoDTO.builder()
                .previewUrl(url)
                .previewType(previewType)
                .previewMode("native")
                .originalUrl(url)
                .converted(false)
                .provider("native")
                .message(message)
                .build();
    }

    private FilePreviewInfoDTO pdfInfo(String previewUrl, String originalUrl) {
        return pdfInfo(previewUrl, originalUrl, "已通过服务端转码为 PDF 预览");
    }

    private FilePreviewInfoDTO pdfInfo(String previewUrl, String originalUrl, String message) {
        return FilePreviewInfoDTO.builder()
                .previewUrl(previewUrl)
                .previewType("document")
                .previewMode("pdf")
                .originalUrl(originalUrl)
                .converted(true)
                .provider("libreoffice")
                .message(message)
                .build();
    }

    private FilePreviewInfoDTO htmlInfo(String previewUrl, String originalUrl) {
        return htmlInfo(previewUrl, originalUrl, "已生成 HTML 摘要预览（版式可能与原文件有差异）");
    }

    private FilePreviewInfoDTO htmlInfo(String previewUrl, String originalUrl, String message) {
        return FilePreviewInfoDTO.builder()
                .previewUrl(previewUrl)
                .previewType("document")
                .previewMode("html")
                .originalUrl(originalUrl)
                .converted(true)
                .provider("poi")
                .message(message)
                .build();
    }

    private String mapPreviewType(String ext) {
        if (Set.of("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm").contains(ext)) {
            return "video";
        }
        if (Set.of("mp3", "wav", "flac", "aac", "ogg", "wma").contains(ext)) {
            return "audio";
        }
        if (Set.of("jpg", "jpeg", "png", "gif", "bmp", "webp").contains(ext)) {
            return "image";
        }
        return "document";
    }

    private boolean isMediaOrImage(String ext) {
        String t = mapPreviewType(ext);
        return "video".equals(t) || "audio".equals(t) || "image".equals(t);
    }

    /** 远程 OSS 文件下载到本地缓存，供 LibreOffice 转 PDF / 幻灯片图预览 */
    private File downloadRemoteFileForPreview(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return null;
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            return null;
        }
        String ext = getExtensionFromUrl(fileUrl);
        if (ext.isBlank()) {
            ext = "bin";
        }
        try {
            String cacheKey = buildCacheKeyFromUrl(fileUrl);
            File cacheDir = new File(uploadPath, "preview-cache/remote");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File cached = new File(cacheDir, cacheKey + "." + ext);
            if (cached.exists() && cached.length() > 0) {
                return cached;
            }

            URL url = URI.create(fileUrl).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(120000);
            connection.setInstanceFollowRedirects(true);
            int status = connection.getResponseCode();
            if (status >= 400) {
                log.warn("远程预览文件下载失败 status={} url={}", status, fileUrl);
                return null;
            }

            try (InputStream in = connection.getInputStream();
                 OutputStream out = Files.newOutputStream(cached.toPath())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
            if (cached.length() > 0) {
                log.info("远程文件已缓存供预览: {}", cached.getName());
                return cached;
            }
            cached.delete();
        } catch (Exception e) {
            log.warn("远程文件下载预览失败 url={} err={}", fileUrl, e.getMessage());
        }
        return null;
    }

    private String buildCacheKeyFromUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 32);
        } catch (Exception e) {
            return Integer.toHexString(url.hashCode());
        }
    }

    private File resolveLocalFile(String fileUrl) {
        String path = fileUrl;
        if (path.startsWith("http://") || path.startsWith("https://")) {
            String prefix = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
            if (path.startsWith(prefix)) {
                path = path.substring(prefix.length());
            } else {
                int uploadsIdx = path.indexOf("/uploads/");
                if (uploadsIdx >= 0) {
                    path = path.substring(uploadsIdx + "/uploads/".length());
                } else {
                    return null;
                }
            }
        }
        path = path.replace("\\", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return new File(uploadPath + File.separator + path.replace("/", File.separator));
    }

    private String toAccessibleUrl(String fileUrlOrPath) {
        if (fileUrlOrPath == null || fileUrlOrPath.isBlank()) {
            return null;
        }
        if (fileUrlOrPath.startsWith("http://") || fileUrlOrPath.startsWith("https://")) {
            return fileUrlOrPath;
        }
        String path = fileUrlOrPath.replace("\\", "/");
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/" + path;
    }

    private String relativePathFromUpload(File file) {
        String upload = new File(uploadPath).getAbsolutePath();
        String absolute = file.getAbsolutePath();
        if (absolute.startsWith(upload)) {
            String rel = absolute.substring(upload.length()).replace("\\", "/");
            if (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            return rel;
        }
        return file.getName();
    }

    private String buildCacheKey(File file) {
        try {
            String seed = file.getAbsolutePath() + "|" + file.length() + "|" + file.lastModified();
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(seed.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash).substring(0, 32);
        } catch (Exception e) {
            return Integer.toHexString(file.getAbsolutePath().hashCode());
        }
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(dot + 1) : "";
    }

    private String getExtensionFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "";
        }
        String path = url;
        int queryIdx = path.indexOf('?');
        if (queryIdx >= 0) {
            path = path.substring(0, queryIdx);
        }
        int slash = path.lastIndexOf('/');
        if (slash >= 0) {
            path = path.substring(slash + 1);
        }
        return getExtension(path).toLowerCase(Locale.ROOT);
    }

    private void deleteDirectory(File dir) {
        if (dir == null || !dir.exists()) {
            return;
        }
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (child.isDirectory()) {
                    deleteDirectory(child);
                } else {
                    child.delete();
                }
            }
        }
        dir.delete();
    }
}
