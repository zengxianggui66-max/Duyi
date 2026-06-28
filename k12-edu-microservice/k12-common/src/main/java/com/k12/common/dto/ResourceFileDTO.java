package com.k12.common.dto;

import lombok.Data;

@Data
public class ResourceFileDTO {

    /** main | answer | seewo | video | audio */
    private String fileRole = "main";

    private String originalFilename;
    private String fileExt;
    private String ossBucket;
    private String ossObjectKey;
    private String ossUrl;
    private Long fileSizeBytes;
    private Integer allowPreview = 1;
    private Integer sort = 0;
}
