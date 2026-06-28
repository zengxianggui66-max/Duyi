package com.k12.prep.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.PrepBasketItemDTO;
import com.k12.common.dto.PrepBasketVO;
import com.k12.common.entity.PrepBasket;
import com.k12.common.entity.PrepBasketItem;
import com.k12.prep.mapper.PrepBasketItemMapper;
import com.k12.prep.mapper.PrepBasketMapper;
import com.k12.prep.service.PrepBasketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrepBasketServiceImpl implements PrepBasketService {

    private final PrepBasketMapper basketMapper;
    private final PrepBasketItemMapper itemMapper;

    @Override
    public PrepBasketVO getBasket(Long userId) {
        PrepBasket basket = ensureDefaultBasket(userId);
        List<PrepBasketItem> items = itemMapper.selectList(new LambdaQueryWrapper<PrepBasketItem>()
                .eq(PrepBasketItem::getBasketId, basket.getId())
                .orderByAsc(PrepBasketItem::getSortOrder)
                .orderByDesc(PrepBasketItem::getCreateTime));
        return toVO(basket, items);
    }

    @Override
    @Transactional
    public PrepBasketItem addItem(Long userId, PrepBasketItemDTO dto) {
        requireUser(userId);
        PrepBasket basket = ensureDefaultBasket(userId);
        if (exists(userId, dto.getItemType(), dto.getRefId())) {
            throw new BusinessException("该资源已在资料篮中");
        }
        int maxOrder = itemMapper.selectCount(new LambdaQueryWrapper<PrepBasketItem>()
                .eq(PrepBasketItem::getBasketId, basket.getId())).intValue();
        PrepBasketItem item = new PrepBasketItem();
        item.setBasketId(basket.getId());
        item.setItemType(dto.getItemType());
        item.setRefId(dto.getRefId());
        item.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : "未命名");
        item.setSubtitle(dto.getSubtitle());
        item.setCoverUrl(dto.getCoverUrl());
        item.setMetaJson(dto.getMetaJson());
        item.setScore(dto.getScore());
        item.setSortOrder(maxOrder + 1);
        itemMapper.insert(item);
        return item;
    }

    @Override
    @Transactional
    public void removeItem(Long userId, Long itemId) {
        requireUser(userId);
        PrepBasket basket = ensureDefaultBasket(userId);
        PrepBasketItem item = itemMapper.selectById(itemId);
        if (item == null || !basket.getId().equals(item.getBasketId())) {
            throw new BusinessException("条目不存在");
        }
        itemMapper.deleteById(itemId);
    }

    @Override
    @Transactional
    public void clear(Long userId) {
        requireUser(userId);
        PrepBasket basket = ensureDefaultBasket(userId);
        itemMapper.delete(new LambdaQueryWrapper<PrepBasketItem>()
                .eq(PrepBasketItem::getBasketId, basket.getId()));
    }

    @Override
    @Transactional
    public void reorder(Long userId, List<Long> orderedItemIds) {
        requireUser(userId);
        PrepBasket basket = ensureDefaultBasket(userId);
        if (orderedItemIds == null || orderedItemIds.isEmpty()) {
            return;
        }
        int order = 0;
        for (Long id : orderedItemIds) {
            PrepBasketItem item = itemMapper.selectById(id);
            if (item != null && basket.getId().equals(item.getBasketId())) {
                item.setSortOrder(order++);
                itemMapper.updateById(item);
            }
        }
    }

    @Override
    @Transactional
    public int mergeItems(Long userId, List<PrepBasketItemDTO> items) {
        requireUser(userId);
        if (items == null || items.isEmpty()) {
            return 0;
        }
        int merged = 0;
        for (PrepBasketItemDTO dto : items) {
            if (dto.getItemType() == null || dto.getRefId() == null) {
                continue;
            }
            if (exists(userId, dto.getItemType(), dto.getRefId())) {
                continue;
            }
            PrepBasket basket = ensureDefaultBasket(userId);
            int maxOrder = itemMapper.selectCount(new LambdaQueryWrapper<PrepBasketItem>()
                    .eq(PrepBasketItem::getBasketId, basket.getId())).intValue();
            PrepBasketItem item = new PrepBasketItem();
            item.setBasketId(basket.getId());
            item.setItemType(dto.getItemType());
            item.setRefId(dto.getRefId());
            item.setTitle(StringUtils.hasText(dto.getTitle()) ? dto.getTitle() : "未命名");
            item.setSubtitle(dto.getSubtitle());
            item.setCoverUrl(dto.getCoverUrl());
            item.setMetaJson(dto.getMetaJson());
            item.setScore(dto.getScore());
            item.setSortOrder(maxOrder + 1);
            itemMapper.insert(item);
            merged++;
        }
        return merged;
    }

    @Override
    public boolean exists(Long userId, String itemType, Long refId) {
        if (userId == null) {
            return false;
        }
        PrepBasket basket = basketMapper.selectOne(new LambdaQueryWrapper<PrepBasket>()
                .eq(PrepBasket::getUserId, userId)
                .eq(PrepBasket::getIsDefault, 1)
                .last("LIMIT 1"));
        if (basket == null) {
            return false;
        }
        return itemMapper.selectCount(new LambdaQueryWrapper<PrepBasketItem>()
                .eq(PrepBasketItem::getBasketId, basket.getId())
                .eq(PrepBasketItem::getItemType, itemType)
                .eq(PrepBasketItem::getRefId, refId)) > 0;
    }

    private PrepBasket ensureDefaultBasket(Long userId) {
        requireUser(userId);
        PrepBasket basket = basketMapper.selectOne(new LambdaQueryWrapper<PrepBasket>()
                .eq(PrepBasket::getUserId, userId)
                .eq(PrepBasket::getIsDefault, 1)
                .last("LIMIT 1"));
        if (basket == null) {
            basket = new PrepBasket();
            basket.setUserId(userId);
            basket.setName("默认资料篮");
            basket.setIsDefault(1);
            basketMapper.insert(basket);
        }
        return basket;
    }

    private static PrepBasketVO toVO(PrepBasket basket, List<PrepBasketItem> items) {
        PrepBasketVO vo = new PrepBasketVO();
        vo.setBasketId(basket.getId());
        vo.setName(basket.getName());
        vo.setItems(items);
        vo.setTotalCount(items.size());
        vo.setResourceCount((int) items.stream().filter(i -> "resource".equals(i.getItemType())).count());
        vo.setQuestionCount((int) items.stream().filter(i -> "question".equals(i.getItemType())).count());
        vo.setPaperCount((int) items.stream().filter(i -> "paper".equals(i.getItemType())).count());
        vo.setAlbumCount((int) items.stream().filter(i -> "album".equals(i.getItemType())).count());
        return vo;
    }

    private static void requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(401, "请先登录后使用资料篮");
        }
    }
}
