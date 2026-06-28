package com.k12.resource.service;

import com.k12.common.dto.HomePreviewVO;

public interface HomePreviewService {
    HomePreviewVO buildPreview(String stageKey);
}
