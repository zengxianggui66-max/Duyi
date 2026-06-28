package com.k12.resource.service;

import com.k12.common.entity.PrimaryChineseResource;
import com.k12.resource.config.SearchProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AuditContentAnalyzer {

    private static final Set<String> RISK_EXTENSIONS = Set.of(
            "exe", "bat", "cmd", "com", "scr", "vbs", "js", "jar", "msi", "dll");
    private static final Set<String> SAFE_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "txt", "jpg", "jpeg", "png", "gif", "webp",
            "mp4", "mp3", "wav", "m4a", "webm");

    private final SearchProperties searchProperties;
    public AuditContentAnalyzer(SearchProperties searchProperties) {
        this.searchProperties = searchProperties;
    }


    public List<String> detectSensitiveWords(PrimaryChineseResource resource) {
        String text = joinText(
                resource.getTitle(),
                resource.getRemark(),
                resource.getOriginalFilename(),
                resource.getUnitName(),
                resource.getLessonName());
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        String lower = text.toLowerCase(Locale.ROOT);
        List<String> hits = new ArrayList<>();
        for (String word : searchProperties.getSensitiveWords()) {
            if (!StringUtils.hasText(word)) {
                continue;
            }
            if (lower.contains(word.toLowerCase(Locale.ROOT))) {
                hits.add(word);
            }
        }
        return hits;
    }

    public FileSafety assessFileSafety(PrimaryChineseResource resource) {
        if (!StringUtils.hasText(resource.getOssUrl())) {
            return new FileSafety("pending", "尚未上传文件，无法评估安全性");
        }
        String ext = normalizeExt(resource.getFileExt());
        if (RISK_EXTENSIONS.contains(ext)) {
            return new FileSafety("risk", "可执行或脚本类文件，建议人工核查后再通过");
        }
        if ("zip".equals(ext) || "rar".equals(ext) || "7z".equals(ext)) {
            return new FileSafety("pending", "压缩包需解压核查内容，建议预览或下载后人工确认");
        }
        if (SAFE_EXTENSIONS.contains(ext)) {
            return new FileSafety("safe", "常见文档/媒体格式，未发现高风险扩展名");
        }
        return new FileSafety("unknown", "非常见格式，建议下载后人工确认");
    }

    private String joinText(String... parts) {
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (StringUtils.hasText(part)) {
                if (sb.length() > 0) {
                    sb.append(' ');
                }
                sb.append(part.trim());
            }
        }
        return sb.toString();
    }

    private String normalizeExt(String ext) {
        if (!StringUtils.hasText(ext)) {
            return "";
        }
        return ext.toLowerCase(Locale.ROOT).replace(".", "");
    }

    public record FileSafety(String status, String message) {}
}
