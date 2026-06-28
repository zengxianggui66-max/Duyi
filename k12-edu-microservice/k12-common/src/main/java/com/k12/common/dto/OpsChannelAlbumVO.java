package com.k12.common.dto;

import lombok.Data;

@Data
public class OpsChannelAlbumVO {
    private Long id;
    private String channelCode;
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
