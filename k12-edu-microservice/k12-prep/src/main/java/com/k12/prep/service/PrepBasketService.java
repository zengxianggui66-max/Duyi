package com.k12.prep.service;

import com.k12.common.dto.PrepBasketItemDTO;
import com.k12.common.dto.PrepBasketVO;
import com.k12.common.entity.PrepBasketItem;

import java.util.List;

public interface PrepBasketService {
    PrepBasketVO getBasket(Long userId);

    PrepBasketItem addItem(Long userId, PrepBasketItemDTO dto);

    void removeItem(Long userId, Long itemId);

    void clear(Long userId);

    void reorder(Long userId, List<Long> orderedItemIds);

    boolean exists(Long userId, String itemType, Long refId);

    /** 合并本地资料篮条目（跳过重复） */
    int mergeItems(Long userId, List<PrepBasketItemDTO> items);
}
