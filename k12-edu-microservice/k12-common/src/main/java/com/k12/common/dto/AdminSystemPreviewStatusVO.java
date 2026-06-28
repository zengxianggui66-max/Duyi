package com.k12.common.dto;

import lombok.Data;

@Data
public class AdminSystemPreviewStatusVO {

    private Boolean enabled;
    private AdminSystemLibreOfficeStatusVO libreoffice;
    private Boolean poiFallback;
    private Boolean asyncEnabled;
    /** skipped / ok / failed */
    private String sampleProbe;
}
