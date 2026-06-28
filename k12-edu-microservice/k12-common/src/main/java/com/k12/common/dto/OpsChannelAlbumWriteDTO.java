package com.k12.common.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpsChannelAlbumWriteDTO {
    @NotBlank
    private String title;
    private String icon;
    private String meta;
    private Integer resourceCount;
    private Integer downloadCount;
    private String coverGradient;
    private String linkPath;
    private Integer sort;
    private Integer status;
}
