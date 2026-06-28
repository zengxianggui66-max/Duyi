package com.k12.common.dto;

import lombok.Data;

@Data
public class OpsChannelAlbumItemVO {
    private Long id;
    private String title;
    private String icon;
    private String meta;
    private Integer resourceCount;
    private Integer downloadCount;
    private String coverColor;
    private String linkPath;
}
