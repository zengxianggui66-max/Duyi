package com.k12.resource.preview;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 将 PDF 按页渲染为 PNG（PPT 预览稳定链路：PPT → PDF → PNG）
 */
@Slf4j
@Component
public class PdfSlideImageRenderer {

    private static final float RENDER_DPI = 150f;

    /**
     * @return 按页排序的 PNG 文件（slide-001.png …）
     */
    public List<File> renderPages(File pdfFile, File outDir) {
        if (pdfFile == null || !pdfFile.exists() || pdfFile.length() <= 0) {
            return List.of();
        }
        if (!outDir.exists() && !outDir.mkdirs()) {
            return List.of();
        }

        try (PDDocument document = Loader.loadPDF(pdfFile)) {
            int pageCount = document.getNumberOfPages();
            if (pageCount <= 0) {
                return List.of();
            }

            PDFRenderer renderer = new PDFRenderer(document);
            List<File> pages = new ArrayList<>(pageCount);
            for (int pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                BufferedImage image = renderer.renderImageWithDPI(pageIndex, RENDER_DPI, ImageType.RGB);
                File png = new File(outDir, String.format(Locale.ROOT, "slide-%03d.png", pageIndex + 1));
                ImageIO.write(image, "png", png);
                pages.add(png);
            }
            log.info("PDF 渲染幻灯片图成功: {} 页, pdf={}", pages.size(), pdfFile.getName());
            return pages;
        } catch (Exception e) {
            log.warn("PDF 渲染幻灯片图失败: {} err={}", pdfFile.getName(), e.getMessage());
            return List.of();
        }
    }
}
