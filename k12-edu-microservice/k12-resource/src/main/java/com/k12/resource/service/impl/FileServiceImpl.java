package com.k12.resource.service.impl;

import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.ResourceUploadDTO;
import com.k12.common.entity.Resource;
import com.k12.resource.service.DocumentPreviewService;
import com.k12.resource.service.FileService;
import com.k12.resource.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Locale;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@SuppressWarnings("null")
public class FileServiceImpl implements FileService {

    @Autowired
    private ResourceService resourceService;

    @Autowired
    private DocumentPreviewService documentPreviewService;

    @Value("${upload.path:${user.home}/k12-uploads}")
    private String uploadPath;

    @Value("${upload.base-url:http://localhost:8080/uploads}")
    private String baseUrl;

    @Value("${upload.max-size:500}")
    private int maxSizeMb;

    // 允许的文件格式
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "doc", "docx", "pdf", "ppt", "pptx", "xls", "xlsx",
            "mp4", "avi", "mov", "wmv", "flv", "mkv", "webm",
            "mp3", "wav", "flac", "aac", "ogg", "wma",
            "jpg", "jpeg", "png", "gif", "bmp", "webp",
            "zip", "rar", "7z"
    );

    // 格式配置
    private static final Map<String, Map<String, Object>> FORMAT_CONFIGS = new HashMap<>();

    static {
        // 文档格式
        Map<String, Object> docConfig = new HashMap<>();
        docConfig.put("name", "文档");
        docConfig.put("icon", "document");
        docConfig.put("maxSizeMb", 50);
        docConfig.put("isPreviewable", true);
        docConfig.put("previewType", "document");
        FORMAT_CONFIGS.put("doc", docConfig);
        FORMAT_CONFIGS.put("docx", docConfig);
        FORMAT_CONFIGS.put("pdf", docConfig);
        
        Map<String, Object> pptConfig = new HashMap<>();
        pptConfig.put("name", "演示文稿");
        pptConfig.put("icon", "ppt");
        pptConfig.put("maxSizeMb", 100);
        pptConfig.put("isPreviewable", true);
        pptConfig.put("previewType", "document");
        FORMAT_CONFIGS.put("ppt", pptConfig);
        FORMAT_CONFIGS.put("pptx", pptConfig);

        Map<String, Object> xlsConfig = new HashMap<>();
        xlsConfig.put("name", "表格");
        xlsConfig.put("icon", "excel");
        xlsConfig.put("maxSizeMb", 50);
        xlsConfig.put("isPreviewable", true);
        xlsConfig.put("previewType", "document");
        FORMAT_CONFIGS.put("xls", xlsConfig);
        FORMAT_CONFIGS.put("xlsx", xlsConfig);

        // 视频格式
        Map<String, Object> videoConfig = new HashMap<>();
        videoConfig.put("name", "视频");
        videoConfig.put("icon", "video");
        videoConfig.put("maxSizeMb", 500);
        videoConfig.put("isPreviewable", true);
        videoConfig.put("previewType", "video");
        for (String ext : Arrays.asList("mp4", "avi", "mov", "wmv", "flv", "mkv", "webm")) {
            FORMAT_CONFIGS.put(ext, videoConfig);
        }

        // 音频格式
        Map<String, Object> audioConfig = new HashMap<>();
        audioConfig.put("name", "音频");
        audioConfig.put("icon", "audio");
        audioConfig.put("maxSizeMb", 100);
        audioConfig.put("isPreviewable", true);
        audioConfig.put("previewType", "audio");
        for (String ext : Arrays.asList("mp3", "wav", "flac", "aac", "ogg", "wma")) {
            FORMAT_CONFIGS.put(ext, audioConfig);
        }

        // 图片格式
        Map<String, Object> imageConfig = new HashMap<>();
        imageConfig.put("name", "图片");
        imageConfig.put("icon", "image");
        imageConfig.put("maxSizeMb", 20);
        imageConfig.put("isPreviewable", true);
        imageConfig.put("previewType", "image");
        for (String ext : Arrays.asList("jpg", "jpeg", "png", "gif", "bmp", "webp")) {
            FORMAT_CONFIGS.put(ext, imageConfig);
        }

        // 压缩格式
        Map<String, Object> zipConfig = new HashMap<>();
        zipConfig.put("name", "压缩包");
        zipConfig.put("icon", "zip");
        zipConfig.put("maxSizeMb", 500);
        zipConfig.put("isPreviewable", false);
        zipConfig.put("previewType", null);
        for (String ext : Arrays.asList("zip", "rar", "7z")) {
            FORMAT_CONFIGS.put(ext, zipConfig);
        }
    }

    @Override
    @Transactional
    public Map<String, Object> uploadFile(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename).toLowerCase();

        // 检查格式是否支持（使用完整文件名，避免仅传扩展名时校验失败）
        if (!isFormatSupported(originalFilename)) {
            throw new IllegalArgumentException("不支持的文件格式: ." + extension);
        }

        // 获取格式配置
        Map<String, Object> config = FORMAT_CONFIGS.get(extension);
        int maxSize = config != null ? (int) config.get("maxSizeMb") : maxSizeMb;

        // 检查文件大小
        long fileSizeMb = file.getSize() / (1024 * 1024);
        if (fileSizeMb > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制: 最大" + maxSize + "MB");
        }

        // 创建存储目录并保存（使用 Files.copy，避免 Windows 跨盘符 transferTo 失败）
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path rootDir = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path targetDir = rootDir.resolve(datePath.replace('/', File.separatorChar));
        Files.createDirectories(targetDir);

        String newFileName = UUID.randomUUID().toString() + "." + extension;
        Path targetFile = targetDir.resolve(newFileName);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String fileUrl = baseUrl + "/" + datePath + "/" + newFileName;
        String relativePath = datePath + "/" + newFileName;

        log.info("文件上传成功: {}, 大小: {} bytes", originalFilename, file.getSize());

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", originalFilename);
        result.put("fileUrl", fileUrl);
        result.put("filePath", relativePath);
        result.put("fileSize", file.getSize());
        result.put("fileFormat", extension);
        result.put("contentType", file.getContentType());
        result.put("isPreviewable", config != null && (boolean) config.get("isPreviewable"));
        result.put("previewType", config != null ? config.get("previewType") : null);
        result.put("formatName", config != null ? config.get("name") : "未知");
        result.put("icon", config != null ? config.get("icon") : "file");
        result.put("ossBucket", "local");
        result.put("ossObjectKey", relativePath);

        return result;
    }

    @Override
    @Transactional
    public Resource uploadResource(MultipartFile file, ResourceUploadDTO uploadDTO) throws Exception {
        // 上传文件
        Map<String, Object> uploadResult = uploadFile(file);

        String extension = (String) uploadResult.get("fileFormat");

        // 构建资源对象
        Resource resource = new Resource();
        resource.setTitle(uploadDTO.getTitle() != null ? uploadDTO.getTitle() : (String) uploadResult.get("fileName"));
        resource.setDescription(uploadDTO.getDescription());
        resource.setGradeLevel(uploadDTO.getGradeLevel());
        resource.setSubject(uploadDTO.getSubject());
        resource.setGrade(uploadDTO.getGrade());
        resource.setVersion(uploadDTO.getVersion());
        resource.setResourceType(uploadDTO.getResourceType());
        resource.setExamType(uploadDTO.getExamType());
        resource.setCategoryId(uploadDTO.getCategoryId());
        resource.setTags(uploadDTO.getTags());
        resource.setIsFree(uploadDTO.getIsFree() != null ? uploadDTO.getIsFree() : 1);

        // 文件信息
        resource.setFileUrl((String) uploadResult.get("fileUrl"));
        resource.setStoragePath((String) uploadResult.get("filePath"));
        resource.setOriginalName((String) uploadResult.get("fileName"));
        resource.setFileSize((Long) uploadResult.get("fileSize"));
        resource.setFileFormat(extension);
        resource.setContentType((String) uploadResult.get("contentType"));
        resource.setIsPreviewable((Boolean) uploadResult.get("isPreviewable") ? 1 : 0);
        resource.setUploadStatus("completed");
        resource.setStorageType("local");
        resource.setAuthorId(uploadDTO.getAuthorId() != null ? uploadDTO.getAuthorId() : 1L);
        resource.setAuthorName(
                uploadDTO.getAuthorName() != null && !uploadDTO.getAuthorName().isBlank()
                        ? uploadDTO.getAuthorName()
                        : "管理员");
        resource.setStatus(1);
        resource.setDownloadCount(0);
        resource.setViewCount(0);
        resource.setCollectCount(0);

        // 创建资源
        resourceService.create(resource);

        return resource;
    }

    @Override
    public List<Map<String, Object>> getSupportedFormats() {
        List<Map<String, Object>> formats = new ArrayList<>();
        Set<String> addedFormats = new HashSet<>();

        for (Map.Entry<String, Map<String, Object>> entry : FORMAT_CONFIGS.entrySet()) {
            String extension = entry.getKey();
            Map<String, Object> config = entry.getValue();
            String formatName = (String) config.get("name");

            // 避免重复添加同类型的格式
            if (!addedFormats.contains(formatName)) {
                Map<String, Object> format = new HashMap<>();
                format.put("extension", extension);
                format.put("name", formatName);
                format.put("icon", config.get("icon"));
                format.put("maxSizeMb", config.get("maxSizeMb"));
                format.put("isPreviewable", config.get("isPreviewable"));
                format.put("previewType", config.get("previewType"));
                formats.add(format);
                addedFormats.add(formatName);
            }
        }

        return formats;
    }

    /**
     * 判断格式是否支持。参数可为完整文件名（a.docx）或纯扩展名（docx）。
     */
    @Override
    public boolean isFormatSupported(String filenameOrExt) {
        if (filenameOrExt == null || filenameOrExt.isBlank()) {
            return false;
        }
        String ext = filenameOrExt.contains(".")
                ? getFileExtension(filenameOrExt)
                : filenameOrExt.trim();
        if (ext.isEmpty()) {
            return false;
        }
        return ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            File file = new File(uploadPath + File.separator + filePath);
            if (file.exists()) {
                return file.delete();
            }
            return true;
        } catch (Exception e) {
            log.error("删除文件失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPreviewUrl(String fileUrl) {
        FilePreviewInfoDTO info = getPreviewInfo(fileUrl);
        return info != null ? info.getPreviewUrl() : null;
    }

    @Override
    public FilePreviewInfoDTO getPreviewInfo(String fileUrl) {
        return documentPreviewService.resolvePreview(fileUrl);
    }

    @Override
    public String getDownloadUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }
        // 如果是完整URL，直接返回
        if (fileUrl.startsWith("http://") || fileUrl.startsWith("https://")) {
            return fileUrl;
        }
        // 如果是相对路径，拼接baseUrl
        return baseUrl + "/" + fileUrl;
    }

    /**
     * 获取文件扩展名
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
