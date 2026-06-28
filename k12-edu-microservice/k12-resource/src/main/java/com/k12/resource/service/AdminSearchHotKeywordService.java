package com.k12.resource.service;

import com.k12.common.dto.AdminSearchHotKeywordVO;

import java.util.List;

public interface AdminSearchHotKeywordService {

    List<AdminSearchHotKeywordVO> list(boolean includeDisabled);

    void setStatus(Long id, Integer status);

    AdminSearchHotKeywordVO updateBoostScore(Long id, Integer boostScore);
}
