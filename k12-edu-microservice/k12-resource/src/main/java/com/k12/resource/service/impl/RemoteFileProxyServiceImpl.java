package com.k12.resource.service.impl;

import com.k12.resource.service.RemoteFileProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@SuppressWarnings("null")
public class RemoteFileProxyServiceImpl implements RemoteFileProxyService {

    private static final int BUFFER_SIZE = 8192;

    @Override
    public void streamUrl(
            String fileUrl,
            String filename,
            String contentType,
            boolean attachment,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (!StringUtils.hasText(fileUrl)) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "文件地址为空");
            return;
        }
        if (!fileUrl.startsWith("http://") && !fileUrl.startsWith("https://")) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "仅支持 http(s) 远程地址");
            return;
        }

        HttpURLConnection connection = null;
        try {
            URL url = URI.create(fileUrl).toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(120000);
            connection.setInstanceFollowRedirects(true);

            String range = request.getHeader("Range");
            if (StringUtils.hasText(range)) {
                connection.setRequestProperty("Range", range);
            }

            int status = connection.getResponseCode();
            if (status >= 400) {
                log.warn("远程文件请求失败 status={} url={}", status, fileUrl);
                writeError(response, status, "远程文件不可访问（HTTP " + status + "）");
                return;
            }

            String resolvedType = StringUtils.hasText(contentType)
                    ? contentType
                    : connection.getContentType();
            if (!StringUtils.hasText(resolvedType)) {
                resolvedType = "application/octet-stream";
            }
            response.setContentType(resolvedType);

            String safeName = StringUtils.hasText(filename) ? filename : "resource";
            String encoded = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replace("+", "%20");
            String disposition = attachment ? "attachment" : "inline";
            response.setHeader("Content-Disposition", disposition + "; filename*=UTF-8''" + encoded);
            response.setHeader("Accept-Ranges", "bytes");

            long contentLength = connection.getContentLengthLong();
            if (contentLength > 0) {
                response.setHeader("Content-Length", String.valueOf(contentLength));
            }

            if (status == HttpURLConnection.HTTP_PARTIAL) {
                response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
                String contentRange = connection.getHeaderField("Content-Range");
                if (StringUtils.hasText(contentRange)) {
                    response.setHeader("Content-Range", contentRange);
                }
            }

            try (InputStream in = connection.getInputStream();
                 OutputStream out = response.getOutputStream()) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                out.flush();
            }
        } catch (Exception e) {
            log.error("代理远程文件失败 url={}", fileUrl, e);
            if (!response.isCommitted()) {
                writeError(response, HttpServletResponse.SC_BAD_GATEWAY, "文件代理失败：" + e.getMessage());
            }
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void writeError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write(message);
        } catch (Exception ignored) {
            // ignore
        }
    }
}
