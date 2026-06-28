package com.k12.common.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OpsChannelBootstrapVO {
    private String code;
    private String name;
    private String icon;
    private String desc;
    private String bgColor;
    private OpsChannelStatsDTO stats;
    private Boolean showGradeFilter;
    private Boolean showSubjectFilter;
    private String eliteTitle;
    private String eliteDesc;
    private List<OpsChannelTabItemVO> mainTabs;
    private List<OpsChannelAlbumItemVO> eliteAlbums;
    private Map<String, String> tabKeywords;
}
