package com.k12.common.dto;

import lombok.Data;

import java.util.List;

@Data
public class HomePreviewVO {
    private HomeHeroVO hero;
    private List<HomeLatestColumnWithItemsVO> latestColumns;
    private HomeFuncChannelsVO funcChannels;
    private Integer opsChannelCount;
}
