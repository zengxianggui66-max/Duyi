package com.k12.prep.service;

import com.k12.common.dto.BasketDownloadSummaryVO;

public interface PrepBasketDownloadService {
    byte[] buildBasketZip(Long userId);

    BasketDownloadSummaryVO getDownloadSummary(Long userId);
}
