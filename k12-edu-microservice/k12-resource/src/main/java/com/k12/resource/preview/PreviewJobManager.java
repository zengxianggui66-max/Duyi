package com.k12.resource.preview;

import com.k12.common.dto.FilePreviewInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * PPT 等重转码任务：首次请求立即返回 processing，后台转码完成后供轮询取结果。
 */
@Slf4j
@Component
public class PreviewJobManager {

    public enum Status {
        PROCESSING,
        READY,
        FAILED
    }

    private static final class JobEntry {
        volatile Status status = Status.PROCESSING;
        volatile FilePreviewInfoDTO result;
        volatile String errorMessage;
        volatile long startedAt = System.currentTimeMillis();
    }

    private final Map<String, JobEntry> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

    public Status getStatus(String jobKey) {
        JobEntry entry = jobs.get(jobKey);
        return entry != null ? entry.status : null;
    }

    public FilePreviewInfoDTO getReadyResult(String jobKey) {
        JobEntry entry = jobs.get(jobKey);
        if (entry != null && entry.status == Status.READY) {
            return entry.result;
        }
        return null;
    }

    public FilePreviewInfoDTO getFailedResult(String jobKey, String originalUrl) {
        JobEntry entry = jobs.get(jobKey);
        if (entry != null && entry.status == Status.FAILED) {
            return FilePreviewInfoDTO.builder()
                    .originalUrl(originalUrl)
                    .previewType("document")
                    .previewMode("none")
                    .converted(false)
                    .provider("libreoffice")
                    .message(entry.errorMessage != null ? entry.errorMessage : "PPT 转码失败")
                    .build();
        }
        return null;
    }

    /** 失败任务超过 maxAgeMs 后清除，允许用户刷新页面重试 */
    public void evictFailedIfStale(String jobKey, long maxAgeMs) {
        JobEntry entry = jobs.get(jobKey);
        if (entry != null
                && entry.status == Status.FAILED
                && System.currentTimeMillis() - entry.startedAt > maxAgeMs) {
            jobs.remove(jobKey);
        }
    }

    public FilePreviewInfoDTO processingInfo(String originalUrl, String message) {
        return FilePreviewInfoDTO.builder()
                .originalUrl(originalUrl)
                .previewType("document")
                .previewMode("processing")
                .converted(false)
                .provider("libreoffice")
                .message(message != null ? message : "正在转码幻灯片，请稍候…")
                .build();
    }

    /**
     * 若已有进行中任务则不再重复提交；否则后台执行 syncResolver。
     */
    public void startIfAbsent(String jobKey, String originalUrl, Supplier<FilePreviewInfoDTO> syncResolver) {
        JobEntry existing = jobs.get(jobKey);
        if (existing != null && existing.status == Status.PROCESSING) {
            return;
        }
        if (existing != null && existing.status == Status.READY) {
            return;
        }
        if (existing != null && existing.status == Status.FAILED) {
            jobs.remove(jobKey, existing);
        }

        JobEntry entry = new JobEntry();
        JobEntry prev = jobs.putIfAbsent(jobKey, entry);
        if (prev != null) {
            if (prev.status == Status.PROCESSING || prev.status == Status.READY) {
                return;
            }
            jobs.put(jobKey, entry);
        }

        executor.submit(() -> {
            try {
                log.info("PPT 异步转码开始: jobKey={}", jobKey);
                FilePreviewInfoDTO result = syncResolver.get();
                entry.result = result;
                if (result != null && isSuccessMode(result.getPreviewMode())) {
                    entry.status = Status.READY;
                    log.info("PPT 异步转码完成: jobKey={} mode={}", jobKey, result.getPreviewMode());
                } else {
                    entry.status = Status.FAILED;
                    entry.errorMessage = result != null && result.getMessage() != null
                            ? result.getMessage()
                            : "PPT 转码未生成可用预览";
                    log.warn("PPT 异步转码无可用预览: jobKey={}", jobKey);
                }
            } catch (Exception e) {
                entry.status = Status.FAILED;
                entry.errorMessage = e.getMessage() != null ? e.getMessage() : "PPT 转码异常";
                log.error("PPT 异步转码失败: jobKey={} err={}", jobKey, e.getMessage());
            }
        });
    }

    private static boolean isSuccessMode(String mode) {
        return "slides".equals(mode) || "pdf".equals(mode);
    }

    @PreDestroy
    public void shutdown() {
        executor.shutdown();
    }
}
