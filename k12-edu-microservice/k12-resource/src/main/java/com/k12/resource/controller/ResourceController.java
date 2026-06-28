package com.k12.resource.controller;

import com.k12.common.PageResult;
import com.k12.common.Result;
import com.k12.common.config.MinioClientWrapper;
import com.k12.common.config.MinioUtil;
import com.k12.common.dto.ResourceQueryDTO;
import com.k12.common.entity.PrimaryChineseResource;
import com.k12.common.entity.Resource;
import com.k12.resource.service.PrimaryChineseResourceService;
import com.k12.resource.service.ResourceService;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@Slf4j
public class ResourceController {

    private final ResourceService resourceService;
    private final PrimaryChineseResourceService primaryChineseResourceService;
    private final MinioUtil minioUtil;
    private final MinioClientWrapper minioClientWrapper;
    public ResourceController(ResourceService resourceService, PrimaryChineseResourceService primaryChineseResourceService, MinioUtil minioUtil, MinioClientWrapper minioClientWrapper) {
        this.resourceService = resourceService;
        this.primaryChineseResourceService = primaryChineseResourceService;
        this.minioUtil = minioUtil;
        this.minioClientWrapper = minioClientWrapper;
    }


    @GetMapping("/list")
    public Result<PageResult<Resource>> listResources(ResourceQueryDTO query) {
        return Result.success(resourceService.listResources(query));
    }

    @GetMapping("/hot")
    public Result<List<Resource>> getHotResources(@RequestParam(defaultValue = "all") String gradeLevel) {
        return Result.success(resourceService.getHotResources(gradeLevel));
    }

    @GetMapping("/recommend")
    public Result<List<Resource>> getRecommendResources() {
        return Result.success(resourceService.getRecommendResources());
    }

    @GetMapping("/detail/{id}")
    public Result<Resource> getDetail(@PathVariable Long id) {
        return Result.success(resourceService.getDetail(id));
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        return Result.success(resourceService.getStats());
    }

    @GetMapping("/search")
    public Result<PageResult<Resource>> search(ResourceQueryDTO query) {
        return Result.success(resourceService.listResources(query));
    }

    /**
     * 通过资源ID下载文件（自动增加下载计数）
     */
    @GetMapping("/download/{id}")
    public void downloadById(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 获取资源信息
            Resource resource = resourceService.getDetail(id);
            if (resource == null || resource.getFileUrl() == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("资源不存在");
                return;
            }
            
            // 增加下载次数
            resourceService.incrementDownloadCount(id);
            
            // 获取文件对象名
            String objectName = extractObjectName(resource.getFileUrl());
            
            // 获取文件流
            InputStream inputStream = minioUtil.getFile(objectName);
            
            // 获取文件信息
            StatObjectResponse stat = minioClientWrapper.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(minioClientWrapper.getBucketName())
                            .object(objectName)
                            .build()
            );
            
            // 设置响应头
            String filename = resource.getTitle() + "." + resource.getFileFormat();
            response.setContentType(stat.contentType());
            response.setHeader("Content-Disposition", 
                    "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
            response.setHeader("Content-Length", String.valueOf(stat.size()));
            
            // 支持断点续传
            String range = request.getHeader("Range");
            if (range != null) {
                handleRangeRequest(request, response, inputStream, stat.size(), filename, stat.contentType());
            } else {
                // 普通下载
                OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
                inputStream.close();
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try {
                response.getWriter().write("下载失败: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    /**
     * 获取资源下载URL（用于前端直接下载）
     */
    @GetMapping("/download-url/{id}")
    public Result<Map<String, Object>> getDownloadUrl(@PathVariable Long id) {
        try {
            Resource resource = resourceService.getDetail(id);
            if (resource == null || resource.getFileUrl() == null) {
                return Result.error("资源不存在");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", resource.getFileUrl());
            result.put("title", resource.getTitle());
            result.put("fileFormat", resource.getFileFormat());
            result.put("fileSize", resource.getFileSize());
            result.put("downloadUrl", "/api/resource/download/" + id);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取下载链接失败: " + e.getMessage());
        }
    }

    /**
     * 预览文件 - 获取文件访问URL
     */
    @GetMapping("/preview/{id}")
    public Result<Map<String, Object>> preview(@PathVariable Long id) {
        try {
            Resource resource = resourceService.getDetail(id);
            if (resource == null || resource.getFileUrl() == null) {
                return Result.error("资源不存在");
            }
            
            String objectName = extractObjectName(resource.getFileUrl());
            String fileUrl = minioUtil.getPublicUrl(objectName);
            
            StatObjectResponse stat = minioClientWrapper.getMinioClient().statObject(
                    StatObjectArgs.builder()
                            .bucket(minioClientWrapper.getBucketName())
                            .object(objectName)
                            .build()
            );
            
            Map<String, Object> result = new HashMap<>();
            result.put("fileUrl", fileUrl);
            result.put("contentType", stat.contentType());
            result.put("size", stat.size());
            result.put("title", resource.getTitle());
            result.put("fileFormat", resource.getFileFormat());
            
            // 判断预览类型
            String previewType = getPreviewType(stat.contentType());
            result.put("previewType", previewType);
            
            return Result.success(result);
        } catch (Exception e) {
            return Result.error("获取预览失败: " + e.getMessage());
        }
    }

    /**
     * 处理 Range 请求（支持断点续传）
     */
    private void handleRangeRequest(HttpServletRequest request, HttpServletResponse response,
                                     InputStream inputStream, long fileSize, String filename, String contentType) throws Exception {
        String rangeHeader = request.getHeader("Range");
        String range = rangeHeader.replace("bytes=", "");
        String[] ranges = range.split("-");
        long start = Long.parseLong(ranges[0]);
        long end = ranges.length > 1 && !ranges[1].isEmpty() ? Long.parseLong(ranges[1]) : fileSize - 1;
        
        long contentLength = end - start + 1;
        
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, StandardCharsets.UTF_8));
        response.setHeader("Content-Length", String.valueOf(contentLength));
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileSize);
        
        // 跳过起始字节
        if (start > 0) {
            inputStream.skip(start);
        }
        
        OutputStream outputStream = response.getOutputStream();
        byte[] buffer = new byte[4096];
        long remaining = contentLength;
        int bytesRead;
        while (remaining > 0 && (bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
            outputStream.write(buffer, 0, bytesRead);
            remaining -= bytesRead;
        }
        outputStream.flush();
        inputStream.close();
    }

    /**
     * 从URL中提取对象名
     */
    private String extractObjectName(String fileUrl) {
        if (fileUrl == null) return null;
        // 如果是完整URL，提取路径部分
        if (fileUrl.startsWith("http")) {
            try {
                java.net.URL url = java.net.URI.create(fileUrl).toURL();
                String path = url.getPath();
                return path.startsWith("/") ? path.substring(1) : path;
            } catch (Exception e) {
                return fileUrl;
            }
        }
        return fileUrl;
    }

    /**
     * 判断预览类型
     */
    private String getPreviewType(String contentType) {
        if (contentType == null) return "unknown";
        if (contentType.contains("pdf")) return "pdf";
        if (contentType.contains("image")) return "image";
        if (contentType.contains("video")) return "video";
        if (contentType.contains("audio")) return "audio";
        if (contentType.contains("word") || contentType.contains("document")) return "word";
        if (contentType.contains("powerpoint") || contentType.contains("presentation")) return "ppt";
        if (contentType.contains("excel") || contentType.contains("spreadsheet")) return "excel";
        return "unknown";
    }

    /**
     * 查询小学语文学科资源（兼容旧接口，建议改用 /api/primary-chinese/list）
     * GET /api/resource/primary-chinese/list
     * 参数：stage, subject, module, type, gradeName, edition, unitName, keyword
     */
    @GetMapping("/primary-chinese/list")
    public Result<List<PrimaryChineseResource>> listPrimaryChineseResources(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String gradeName,
            @RequestParam(required = false) String edition,
            @RequestParam(required = false) String unitName,
            @RequestParam(required = false) String keyword
    ) {
        try {
            List<PrimaryChineseResource> list = primaryChineseResourceService.findByCondition(
                    stage, subject, module, type, gradeName, edition, unitName);
            return Result.success(list);
        } catch (Exception e) {
            log.error("查询小学语文学科资源失败", e);
            return Result.error("查询失败：" + e.getMessage());
        }
    }
}
