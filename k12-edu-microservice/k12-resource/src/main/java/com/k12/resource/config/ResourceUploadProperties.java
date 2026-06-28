package com.k12.resource.config;

import com.k12.common.constant.ResourceStatusConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 同步备课资源上传默认状态（无管理端时 dev 可设为已发布）
 */
@Data
@Component
@ConfigurationProperties(prefix = "k12.resource.upload")
public class ResourceUploadProperties {

    /**
     * 新建/提交后的默认 status：0=待审核，1=已发布
     * 开发/生产默认待审核；仅本地联调可临时改为 1
     */
    private int defaultStatus = ResourceStatusConstants.PENDING;
}
