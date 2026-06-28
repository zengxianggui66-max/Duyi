package com.k12.resource.service;

import com.k12.common.dto.HomeFuncChannelVO;
import com.k12.common.dto.HomeFuncChannelsVO;
import com.k12.common.dto.PromotionExamTabVO;
import com.k12.common.entity.HomeFuncChannel;
import com.k12.resource.mapper.HomeFuncChannelMapper;
import com.k12.resource.mapper.HomePanelTabConfigMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class HomeFuncChannelService {

    private final HomeFuncChannelMapper funcChannelMapper;
    private final HomePanelTabConfigMapper tabConfigMapper;
    public HomeFuncChannelService(HomeFuncChannelMapper funcChannelMapper, HomePanelTabConfigMapper tabConfigMapper) {
        this.funcChannelMapper = funcChannelMapper;
        this.tabConfigMapper = tabConfigMapper;
    }


    public HomeFuncChannelsVO listFuncChannels() {
        List<HomeFuncChannel> rows = funcChannelMapper.findActiveOrdered();
        HomeFuncChannelsVO result = new HomeFuncChannelsVO();
        if (rows == null || rows.isEmpty()) {
            return result;
        }

        LinkedHashMap<String, List<String>> topicsMap = new LinkedHashMap<>();
        List<HomeFuncChannelVO> channels = new ArrayList<>();
        List<PromotionExamTabVO> examTabs = new ArrayList<>();
        Set<String> seenExamTypes = new LinkedHashSet<>();

        for (HomeFuncChannel row : rows) {
            List<String> topics = tabConfigMapper.listPromotionTopicLabels(row.getExamType());
            if (topics == null) {
                topics = List.of();
            }
            topicsMap.putIfAbsent(row.getExamType(), topics);

            HomeFuncChannelVO vo = new HomeFuncChannelVO();
            vo.setKey(row.getFuncKey());
            vo.setName(row.getName());
            vo.setExamType(row.getExamType());
            vo.setDefaultTopic(row.getDefaultTopic());
            vo.setStageKey(row.getStageKey());
            vo.setPaperTab(row.getPaperTab());
            vo.setPaperDefaultGrade(row.getPaperDefaultGrade());
            vo.setScrollTarget(StringUtils.hasText(row.getScrollTarget()) ? row.getScrollTarget() : "exam-module");
            vo.setBrowseModule(row.getBrowseModule());
            vo.setBrowseStageKey(row.getBrowseStageKey());
            vo.setBrowseDefaultVolume(row.getBrowseDefaultVolume());
            vo.setTopics(new ArrayList<>(topics));
            channels.add(vo);

            if (seenExamTypes.add(row.getExamType())) {
                String label = StringUtils.hasText(row.getExamTabLabel()) ? row.getExamTabLabel() : row.getName();
                examTabs.add(new PromotionExamTabVO(row.getExamType(), label));
            }
        }

        result.setChannels(channels);
        result.setPromotionExamTabs(examTabs);
        result.setPromotionTopicsMap(topicsMap);
        return result;
    }
}
