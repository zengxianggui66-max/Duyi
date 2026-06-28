package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomeHeroVO {

    private List<HomeBannerVO> banners;

    private List<HomeQuickEntryVO> quickEntries;

    private List<HomeHotWordVO> hotWords;
}
