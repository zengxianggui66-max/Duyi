package com.k12.resource.service;

import com.k12.common.dto.*;

import java.util.List;

public interface HomeLatestColumnService {

    List<HomeLatestColumnWithItemsVO> listPublicColumns(String stageKey);

    List<HomeLatestColumnVO> listAdminColumns(boolean includeDisabled);

    HomeLatestColumnVO updateColumn(Long id, HomeLatestColumnWriteDTO dto, Long adminUserId);

    void updateColumnStatus(Long id, int status, Long adminUserId);

    List<HomeLatestItemVO> listManualItems(Long columnId, boolean includeDisabled);

    HomeLatestItemVO createManualItem(Long columnId, HomeLatestItemWriteDTO dto, Long adminUserId);

    HomeLatestItemVO updateManualItem(Long itemId, HomeLatestItemWriteDTO dto, Long adminUserId);

    void deleteManualItem(Long itemId, Long adminUserId);

    List<HomeLatestItemVO> previewColumn(Long columnId, String stageKey);
}
