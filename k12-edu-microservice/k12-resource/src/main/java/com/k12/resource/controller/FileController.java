package com.k12.resource.controller;

import com.k12.common.Result;
import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.ResourceUploadDTO;
import com.k12.common.entity.Resource;
import com.k12.resource.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService fileService;
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }


    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public Result<Map<String, Object>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        try {
            Map<String, Object> result = fileService.uploadFile(file);
            return Result.success(result);
        } catch (Exception e) {
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return Result.fail("上传失败: " + detail + "。请确认 upload.path 目录可写且服务已重启");
        }
    }

    /**
     * 上传文件并创建资源
     */
    @PostMapping("/upload-resource")
    public Result<Resource> uploadResource(
            @RequestParam("file") MultipartFile file,
            @RequestHeader(value = "X-User-Id", required = false) Long userId,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "gradeLevel", required = false) String gradeLevel,
            @RequestParam(value = "subject", required = false) String subject,
            @RequestParam(value = "resourceType", required = false) String resourceType,
            @RequestParam(value = "isFree", defaultValue = "1") Integer isFree) {
        if (file.isEmpty()) {
            return Result.fail("请选择要上传的文件");
        }
        try {
            ResourceUploadDTO uploadDTO = new ResourceUploadDTO();
            uploadDTO.setTitle(title);
            uploadDTO.setDescription(description);
            uploadDTO.setGradeLevel(gradeLevel);
            uploadDTO.setSubject(subject);
            uploadDTO.setResourceType(resourceType);
            uploadDTO.setIsFree(isFree);
            uploadDTO.setAuthorId(userId);
            uploadDTO.setAuthorName(username);
            Resource resource = fileService.uploadResource(file, uploadDTO);
            return Result.success(resource);
        } catch (Exception e) {
            return Result.fail("上传失败: " + e.getMessage());
        }
    }

    /**
     * 获取支持的文件格式
     */
    @GetMapping("/formats")
    public Result<List<Map<String, Object>>> getSupportedFormats() {
        return Result.success(fileService.getSupportedFormats());
    }

    /**
     * 检查文件格式是否支持
     */
    @GetMapping("/check-format")
    public Result<Boolean> checkFormat(@RequestParam String filename) {
        boolean supported = fileService.isFormatSupported(filename);
        return Result.success(supported);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete")
    public Result<Boolean> deleteFile(@RequestParam String filePath) {
        try {
            fileService.deleteFile(filePath);
            return Result.success(true);
        } catch (Exception e) {
            return Result.fail("删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预览URL（兼容旧接口，返回最佳预览地址）
     */
    @GetMapping("/preview")
    public Result<String> getPreviewUrl(@RequestParam String fileUrl) {
        try {
            String previewUrl = fileService.getPreviewUrl(fileUrl);
            return Result.success(previewUrl);
        } catch (Exception e) {
            return Result.fail("获取预览地址失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件预览详情（阶段 C：转码 PDF / POI HTML / 嵌入兜底）
     */
    @GetMapping("/preview-info")
    public Result<FilePreviewInfoDTO> getPreviewInfo(@RequestParam String fileUrl) {
        try {
            return Result.success(fileService.getPreviewInfo(fileUrl));
        } catch (Exception e) {
            return Result.fail("获取预览信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取文件下载URL
     */
    @GetMapping("/download")
    public Result<String> getDownloadUrl(@RequestParam String fileUrl) {
        try {
            String downloadUrl = fileService.getDownloadUrl(fileUrl);
            return Result.success(downloadUrl);
        } catch (Exception e) {
            return Result.fail("获取下载地址失败: " + e.getMessage());
        }
    }
}
