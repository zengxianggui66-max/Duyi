package com.k12.resource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文档预览转码配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "upload.preview")
public class PreviewProperties {

    /** 是否启用服务端预览解析 */
    private boolean enabled = true;

    /** 是否尝试 LibreOffice 转 PDF */
    private boolean libreofficeEnabled = true;

    /** LibreOffice soffice 可执行文件路径，留空则自动探测 */
    private String libreofficePath = "";

    /** LibreOffice 转换超时（秒） */
    private int libreofficeTimeoutSeconds = 120;

    /** LibreOffice 不可用时，非 PPT 文档是否使用 POI 生成 HTML 预览 */
    private boolean poiFallbackEnabled = true;

  /** PPT 是否允许 POI HTML 摘要兜底（生产建议 false） */
    private boolean pptPoiFallbackEnabled = false;

    /** Word(doc/docx) 是否允许 POI HTML 兜底（建议 false，统一走 PDF） */
    private boolean docPoiFallbackEnabled = false;

    /** 是否为 PPT 导出逐页 PNG（阶段 D，PPT→PDF→PNG） */
    private boolean pptSlidesEnabled = true;

    /** PPT 首次预览是否异步转码（避免 HTTP 长时间阻塞） */
    private boolean asyncEnabled = true;

    /** 前端轮询间隔建议（毫秒，仅文档说明） */
    private int asyncPollIntervalMs = 3000;
}
