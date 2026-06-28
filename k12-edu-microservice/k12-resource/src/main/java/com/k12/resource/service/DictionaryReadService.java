package com.k12.resource.service;

import com.k12.common.dto.DictionaryItemVO;

import java.util.List;

/**
 * Phase 5-D：字典/标签统一读服务
 */
public interface DictionaryReadService {

    List<DictionaryItemVO> listExamScenes(boolean includeDisabled);

    List<DictionaryItemVO> listTeachingScenes(boolean includeDisabled);

    List<DictionaryItemVO> listFileFormats(boolean includeDisabled);

    List<DictionaryItemVO> listRegions(Integer parentId, boolean includeDisabled);

    List<DictionaryItemVO> listBrowseTags(String stage, String module, boolean includeDisabled);
}
