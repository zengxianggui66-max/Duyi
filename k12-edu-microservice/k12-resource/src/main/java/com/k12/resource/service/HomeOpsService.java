package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

public interface HomeOpsService {

    List<HomeBannerVO> listBanners(String slotCode, String stage, boolean publicOnly);

    List<HomeQuickEntryVO> listQuickEntries(String stage, boolean publicOnly);

    List<HomeHotWordVO> listHotWords(String stage, boolean publicOnly);

    HomeHeroVO getHero(String slotCode, String stage);

    // Admin
    HomeBannerVO createBanner(HomeBannerWriteDTO dto, Long adminUserId);

    HomeBannerVO updateBanner(Long id, HomeBannerWriteDTO dto, Long adminUserId);

    void updateBannerStatus(Long id, int status, Long adminUserId);

    void deleteBanner(Long id, Long adminUserId);

    HomeQuickEntryVO createQuickEntry(HomeQuickEntryWriteDTO dto, Long adminUserId);

    HomeQuickEntryVO updateQuickEntry(Long id, HomeQuickEntryWriteDTO dto, Long adminUserId);

    void updateQuickEntryStatus(Long id, int status, Long adminUserId);

    void deleteQuickEntry(Long id, Long adminUserId);

    HomeHotWordVO createHotWord(HomeHotWordWriteDTO dto, Long adminUserId);

    HomeHotWordVO updateHotWord(Long id, HomeHotWordWriteDTO dto, Long adminUserId);

    void updateHotWordStatus(Long id, int status, Long adminUserId);

    void deleteHotWord(Long id, Long adminUserId);

    void reorderHotWords(HomeHotWordReorderDTO dto, Long adminUserId);
}
