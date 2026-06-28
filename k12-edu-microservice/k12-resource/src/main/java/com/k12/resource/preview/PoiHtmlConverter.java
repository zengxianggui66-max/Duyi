package com.k12.resource.preview;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * 使用 Apache POI 将 Office 文档转为简易 HTML（LibreOffice 不可用时的兜底）
 */
@Slf4j
@Component
public class PoiHtmlConverter {

    private static final int MAX_EXCEL_ROWS = 200;
    private static final int MAX_EXCEL_COLS = 30;

    public String convertToHtml(File file, String extension) {
        String ext = extension.toLowerCase(Locale.ROOT);
        try {
            return switch (ext) {
                case "docx" -> docxToHtml(file);
                case "doc" -> docToHtml(file);
                case "pptx" -> pptxToHtml(file);
                case "ppt" -> pptToHtml(file);
                case "xlsx" -> xlsxToHtml(file);
                case "xls" -> xlsToHtml(file);
                default -> null;
            };
        } catch (Exception e) {
            log.warn("POI HTML 转换失败 {}: {}", file.getName(), e.getMessage());
            return null;
        }
    }

    private String docxToHtml(File file) throws Exception {
        StringBuilder body = new StringBuilder();
        try (InputStream in = new FileInputStream(file);
             XWPFDocument doc = new XWPFDocument(in)) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText();
                if (text == null || text.isBlank()) {
                    continue;
                }
                body.append("<p>").append(escape(text)).append("</p>\n");
            }
        }
        return wrapHtml(file.getName(), body.toString());
    }

    private String docToHtml(File file) throws Exception {
        try (InputStream in = new FileInputStream(file);
             HWPFDocument doc = new HWPFDocument(in);
             WordExtractor extractor = new WordExtractor(doc)) {
            String[] paragraphs = extractor.getParagraphText();
            StringBuilder body = new StringBuilder();
            if (paragraphs != null) {
                for (String p : paragraphs) {
                    if (p == null || p.isBlank()) {
                        continue;
                    }
                    body.append("<p>").append(escape(p.trim())).append("</p>\n");
                }
            }
            return wrapHtml(file.getName(), body.toString());
        }
    }

    private String pptxToHtml(File file) throws Exception {
        StringBuilder body = new StringBuilder();
        try (InputStream in = new FileInputStream(file);
             XMLSlideShow ppt = new XMLSlideShow(in)) {
            int index = 1;
            for (XSLFSlide slide : ppt.getSlides()) {
                body.append("<section class=\"slide\"><h3>第 ").append(index++).append(" 页</h3>");
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            for (String line : text.split("\\R")) {
                                if (!line.isBlank()) {
                                    body.append("<p>").append(escape(line.trim())).append("</p>");
                                }
                            }
                        }
                    }
                }
                body.append("</section>\n");
            }
        }
        return wrapHtml(file.getName(), body.toString());
    }

    private String pptToHtml(File file) throws Exception {
        StringBuilder body = new StringBuilder();
        try (InputStream in = new FileInputStream(file);
             HSLFSlideShow ppt = new HSLFSlideShow(in)) {
            int index = 1;
            for (var slide : ppt.getSlides()) {
                body.append("<section class=\"slide\"><h3>第 ").append(index++).append(" 页</h3>");
                for (var shape : slide.getShapes()) {
                    if (shape instanceof org.apache.poi.hslf.usermodel.HSLFTextShape textShape) {
                        String text = textShape.getText();
                        if (text != null && !text.isBlank()) {
                            body.append("<p>").append(escape(text.trim())).append("</p>");
                        }
                    }
                }
                body.append("</section>\n");
            }
        }
        return wrapHtml(file.getName(), body.toString());
    }

    private String xlsxToHtml(File file) throws Exception {
        try (InputStream in = new FileInputStream(file);
             XSSFWorkbook wb = new XSSFWorkbook(in)) {
            return sheetWorkbookToHtml(file.getName(), wb);
        }
    }

    private String xlsToHtml(File file) throws Exception {
        try (InputStream in = new FileInputStream(file);
             HSSFWorkbook wb = new HSSFWorkbook(in)) {
            return sheetWorkbookToHtml(file.getName(), wb);
        }
    }

    private String sheetWorkbookToHtml(String title, Workbook wb) {
        Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
        if (sheet == null) {
            return wrapHtml(title, "<p>（空表格）</p>");
        }
        StringBuilder body = new StringBuilder();
        body.append("<h3>").append(escape(sheet.getSheetName())).append("</h3><table>");
        int lastRow = Math.min(sheet.getLastRowNum(), MAX_EXCEL_ROWS);
        for (int r = 0; r <= lastRow; r++) {
            Row row = sheet.getRow(r);
            body.append("<tr>");
            int cols = row == null ? 0 : Math.min(row.getLastCellNum(), MAX_EXCEL_COLS);
            for (int c = 0; c < cols; c++) {
                Cell cell = row == null ? null : row.getCell(c);
                body.append("<td>").append(escape(cellText(cell))).append("</td>");
            }
            body.append("</tr>");
        }
        body.append("</table>");
        return wrapHtml(title, body.toString());
    }

    private String cellText(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> DateUtil.isCellDateFormatted(cell)
                    ? cell.getLocalDateTimeCellValue().toString()
                    : String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    private String wrapHtml(String title, String body) {
        if (body == null || body.isBlank()) {
            body = "<p>未能提取文档正文，请下载原文件查看。</p>";
        }
        return """
                <!DOCTYPE html>
                <html lang="zh-CN">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1"/>
                  <title>%s</title>
                  <style>
                    body { font-family: "Microsoft YaHei", sans-serif; margin: 24px; color: #303133; line-height: 1.75; }
                    h3 { margin: 1.2em 0 0.6em; font-size: 16px; }
                    table { border-collapse: collapse; width: 100%%; margin-bottom: 1em; font-size: 13px; }
                    td, th { border: 1px solid #dcdfe6; padding: 6px 10px; }
                    .slide { margin-bottom: 1.5em; padding-bottom: 1em; border-bottom: 1px dashed #e4e7ed; }
                    p { margin: 0 0 0.6em; }
                  </style>
                </head>
                <body>
                %s
                </body>
                </html>
                """.formatted(escape(title), body);
    }

    private String escape(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
