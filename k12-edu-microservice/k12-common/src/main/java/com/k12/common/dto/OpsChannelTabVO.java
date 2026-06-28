package com.k12.common.dto;

import lombok.Data;

@Data
public class OpsChannelTabVO {
    private Long id;
    private String channelCode;
    private String tabKey;
    private String tabName;
    private String icon;
    private String searchKeyword;
    private Integer sort;
    private Integer status;
}
