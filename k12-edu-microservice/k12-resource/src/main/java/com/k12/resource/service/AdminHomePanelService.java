package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

public interface AdminHomePanelService {

    List<HomePanelTabConfigVO> listTabConfigs(String panelCode, boolean includeDisabled);

    HomePanelTabConfigVO updateTabConfig(Long id, HomePanelTabConfigWriteDTO dto, Long adminUserId);

    void updateTabConfigStatus(Long id, int status, Long adminUserId);

    List<HomePanelFeaturedVO> listFeatured(
            String panelCode, String tabKey, String filterKey, boolean includeDisabled);

    HomePanelFeaturedVO createFeatured(HomePanelFeaturedWriteDTO dto, Long adminUserId);

    HomePanelFeaturedVO updateFeatured(Long id, HomePanelFeaturedWriteDTO dto, Long adminUserId);

    void updateFeaturedStatus(Long id, int status, Long adminUserId);

    void deleteFeatured(Long id, Long adminUserId);

    HomePanelListVO previewPanel(
            String panelCode,
            String tabKey,
            String filterKey,
            String stageKey,
            String subjectName,
            String gradeName,
            Integer limit);
}
