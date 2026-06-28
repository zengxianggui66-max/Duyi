package com.k12.resource.preview;

import com.k12.resource.config.PreviewProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用 LibreOffice headless 将 Office 文档转为 PDF
 */
@Slf4j
@Component
public class LibreOfficeConverter {

    private final PreviewProperties previewProperties;
    public LibreOfficeConverter(PreviewProperties previewProperties) {
        this.previewProperties = previewProperties;
    }


    private volatile String resolvedSofficePath;

    public boolean isAvailable() {
        return resolveSofficePath() != null;
    }

    public String getSofficePath() {
        return resolveSofficePath();
    }

    /**
     * @param source 源文件
     * @param outDir 输出目录（PDF 将生成在同名 .pdf）
     * @return 生成的 PDF 文件，失败返回 null
     */
    public File convertToPdf(File source, File outDir) {
        String soffice = resolveSofficePath();
        if (soffice == null) {
            return null;
        }
        if (!source.exists() || !source.isFile()) {
            return null;
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            return null;
        }

        String baseName = source.getName();
        int dot = baseName.lastIndexOf('.');
        String pdfName = (dot > 0 ? baseName.substring(0, dot) : baseName) + ".pdf";
        File expectedPdf = new File(outDir, pdfName);

        try {
            List<String> cmd = new ArrayList<>();
            cmd.add(soffice);
            cmd.add("--headless");
            cmd.add("--invisible");
            cmd.add("--nologo");
            cmd.add("--nofirststartwizard");
            cmd.add("--convert-to");
            cmd.add("pdf");
            cmd.add("--outdir");
            cmd.add(outDir.getAbsolutePath());
            cmd.add(source.getAbsolutePath());

            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(
                    previewProperties.getLibreofficeTimeoutSeconds(),
                    TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                log.warn("LibreOffice 转换超时: {}", source.getName());
                return null;
            }
            if (process.exitValue() != 0) {
                log.warn("LibreOffice 转换失败 exitCode={} file={}", process.exitValue(), source.getName());
                return null;
            }
            if (expectedPdf.exists() && expectedPdf.length() > 0) {
                return expectedPdf;
            }
            // 部分版本输出文件名与预期不一致，扫描目录
            File[] pdfs = outDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));
            if (pdfs != null && pdfs.length > 0) {
                File newest = pdfs[0];
                for (File f : pdfs) {
                    if (f.lastModified() >= newest.lastModified()) {
                        newest = f;
                    }
                }
                return newest;
            }
        } catch (Exception e) {
            log.warn("LibreOffice 转换异常: {}", e.getMessage());
        }
        return null;
    }

    private String resolveSofficePath() {
        if (resolvedSofficePath != null) {
            return resolvedSofficePath.isEmpty() ? null : resolvedSofficePath;
        }
        synchronized (this) {
            if (resolvedSofficePath != null) {
                return resolvedSofficePath.isEmpty() ? null : resolvedSofficePath;
            }
            String configured = previewProperties.getLibreofficePath();
            if (configured != null && !configured.isBlank()) {
                File f = new File(configured.trim());
                if (f.exists()) {
                    resolvedSofficePath = f.getAbsolutePath();
                    return resolvedSofficePath;
                }
            }
            String[] candidates = {
                    "soffice",
                    "soffice.exe",
                    "C:\\Program Files\\LibreOffice\\program\\soffice.exe",
                    "C:\\Program Files (x86)\\LibreOffice\\program\\soffice.exe",
                    "/usr/bin/soffice",
                    "/usr/bin/libreoffice",
                    "/Applications/LibreOffice.app/Contents/MacOS/soffice"
            };
            for (String candidate : candidates) {
                if (isExecutableAvailable(candidate)) {
                    resolvedSofficePath = candidate;
                    log.info("LibreOffice 可执行文件: {}", resolvedSofficePath);
                    return resolvedSofficePath;
                }
            }
            resolvedSofficePath = "";
            log.info("未检测到 LibreOffice，Office 预览将使用 POI HTML 或嵌入兜底");
            return null;
        }
    }

    private boolean isExecutableAvailable(String path) {
        try {
            if (path.contains(File.separator) || path.contains("/")) {
                File file = new File(path);
                if (!file.exists()) {
                    return false;
                }
                ProcessBuilder pb = new ProcessBuilder(file.getAbsolutePath(), "--version");
                Process p = pb.start();
                return p.waitFor(8, TimeUnit.SECONDS) && p.exitValue() == 0;
            }
            ProcessBuilder pb = new ProcessBuilder(path, "--version");
            Process p = pb.start();
            return p.waitFor(8, TimeUnit.SECONDS) && p.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
