package com.k12.resource.preview;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * PPT/PPTX → PDF（LibreOffice）→ 逐页 PNG（PDFBox），替代不稳定的 soffice --convert-to png
 */
@Slf4j
@Component
public class PptSlideImageExporter {

    private final LibreOfficeConverter libreOfficeConverter;
    private final PdfSlideImageRenderer pdfSlideImageRenderer;
    public PptSlideImageExporter(LibreOfficeConverter libreOfficeConverter, PdfSlideImageRenderer pdfSlideImageRenderer) {
        this.libreOfficeConverter = libreOfficeConverter;
        this.pdfSlideImageRenderer = pdfSlideImageRenderer;
    }


    /**
     * @param existingPdf 已缓存的 PDF（优先使用，避免重复转码）
     * @return 按页码排序的 PNG 文件列表
     */
    public List<File> exportSlideImages(File source, File outDir, File existingPdf) {
        if (source == null || !source.exists() || !libreOfficeConverter.isAvailable()) {
            return List.of();
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            return List.of();
        }

        File pdf = existingPdf;
        File tempPdfDir = null;
        if (pdf == null || !pdf.exists() || pdf.length() <= 0) {
            File parent = outDir.getParentFile() != null ? outDir.getParentFile() : outDir;
            tempPdfDir = new File(parent, "tmp-pdf-slides-" + System.nanoTime());
            if (!tempPdfDir.mkdirs()) {
                return List.of();
            }
            pdf = libreOfficeConverter.convertToPdf(source, tempPdfDir);
            if (pdf == null || !pdf.exists()) {
                deleteDirectory(tempPdfDir);
                log.warn("PPT 转 PDF 失败，无法导出幻灯片图: {}", source.getName());
                return List.of();
            }
        }

        try {
            return pdfSlideImageRenderer.renderPages(pdf, outDir);
        } finally {
            if (tempPdfDir != null) {
                deleteDirectory(tempPdfDir);
            }
        }
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
