package com.k12.prep.util;

import java.nio.charset.StandardCharsets;

/**
 * 生成 Word 可打开的 HTML 文档（.doc）
 */
public final class WordExportHelper {

    private WordExportHelper() {
    }

    public static byte[] toDocBytes(String title, String bodyHtml) {
        String safeTitle = title == null ? "试卷" : title.replace("\"", "'");
        String doc = """
                <html xmlns:o="urn:schemas-microsoft-com:office:office"
                      xmlns:w="urn:schemas-microsoft-com:office:word"
                      xmlns="http://www.w3.org/TR/REC-html40">
                <head>
                <meta charset="utf-8"/>
                <title>%s</title>
                <!--[if gte mso 9]><xml><w:WordDocument><w:View>Print</w:View></w:WordDocument></xml><![endif]-->
                <style>
                body { font-family: SimSun, serif; font-size: 12pt; line-height: 1.6; }
                h1 { text-align: center; font-size: 18pt; }
                h2 { font-size: 14pt; margin-top: 16pt; }
                .q-item { margin: 8pt 0; }
                ul { margin: 4pt 0 4pt 20pt; }
                </style>
                </head>
                <body>%s</body>
                </html>
                """.formatted(safeTitle, bodyHtml == null ? "" : bodyHtml);
        return doc.getBytes(StandardCharsets.UTF_8);
    }
}
