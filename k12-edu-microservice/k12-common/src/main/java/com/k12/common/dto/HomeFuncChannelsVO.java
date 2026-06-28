package com.k12.common.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
public class HomeFuncChannelsVO {

    private List<HomeFuncChannelVO> channels = new ArrayList<>();
    private List<PromotionExamTabVO> promotionExamTabs = new ArrayList<>();
    private Map<String, List<String>> promotionTopicsMap = new LinkedHashMap<>();
}
