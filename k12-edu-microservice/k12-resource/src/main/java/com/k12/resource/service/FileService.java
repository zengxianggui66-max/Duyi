package com.k12.resource.service;

import com.k12.common.dto.FilePreviewInfoDTO;
import com.k12.common.dto.ResourceUploadDTO;
import com.k12.common.entity.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 上传文件
     * @param file 文件
     * @return 上传结果
     */
    Map<String, Object> uploadFile(MultipartFile file) throws Exception;

    /**
     * 上传文件并创建资源
     * @param file 文件
     * @param uploadDTO 上传信息
     * @return 创建的资源
     */
    Resource uploadResource(MultipartFile file, ResourceUploadDTO uploadDTO) throws Exception;

    /**
     * 获取支持的文件格式
     * @return 格式列表
     */
    List<Map<String, Object>> getSupportedFormats();

    /**
     * 检查文件格式是否支持
     * @param filename 文件名
     * @return 是否支持
     */
    boolean isFormatSupported(String filename);

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否成功
     */
    boolean deleteFile(String filePath);

    /**
     * 获取文件预览URL
     * @param fileUrl 文件URL或路径
     * @return 预览URL
     */
    String getPreviewUrl(String fileUrl);

    /**
     * 解析文件预览信息（含 Office 转码）
     */
    FilePreviewInfoDTO getPreviewInfo(String fileUrl);

    /**
     * 获取文件下载URL
     * @param fileUrl 文件URL或路径
     * @return 下载URL
     */
    String getDownloadUrl(String fileUrl);
}
