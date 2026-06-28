package com.k12.resource.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 远程文件（OSS 公网 URL）代理流式输出，解决浏览器 CORS 与下载问题
 */
public interface RemoteFileProxyService {

    void streamUrl(
            String fileUrl,
            String filename,
            String contentType,
            boolean attachment,
            HttpServletRequest request,
            HttpServletResponse response);
}
