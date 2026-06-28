package com.k12.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.k12.common.BusinessException;
import com.k12.common.dto.AdminSearchHotKeywordVO;
import com.k12.resource.entity.SearchHotKeyword;
import com.k12.resource.mapper.SearchHotKeywordMapper;
import com.k12.resource.search.SearchCacheService;
import com.k12.resource.service.AdminSearchHotKeywordService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@SuppressWarnings("null")
public class AdminSearchHotKeywordServiceImpl implements AdminSearchHotKeywordService {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SearchHotKeywordMapper hotKeywordMapper;
    private final SearchCacheService searchCacheService;
    public AdminSearchHotKeywordServiceImpl(SearchHotKeywordMapper hotKeywordMapper, SearchCacheService searchCacheService) {
        this.hotKeywordMapper = hotKeywordMapper;
        this.searchCacheService = searchCacheService;
    }


    @Override
    public List<AdminSearchHotKeywordVO> list(boolean includeDisabled) {
        LambdaQueryWrapper<SearchHotKeyword> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SearchHotKeyword::getDeleted, 0);
        if (!includeDisabled) {
            wrapper.eq(SearchHotKeyword::getStatus, 1);
        }
        List<SearchHotKeyword> rows = hotKeywordMapper.selectList(wrapper);
        rows.sort(Comparator
                .comparingInt(this::effectiveScore).reversed()
                .thenComparing(SearchHotKeyword::getSearchCount, Comparator.reverseOrder())
                .thenComparing(SearchHotKeyword::getId));

        List<AdminSearchHotKeywordVO> result = new ArrayList<>();
        int rank = 1;
        for (SearchHotKeyword row : rows) {
            AdminSearchHotKeywordVO vo = toVo(row);
            vo.setRank(rank++);
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional
    public void setStatus(Long id, Integer status) {
        SearchHotKeyword row = requireRow(id);
        row.setStatus(normalizeStatus(status));
        hotKeywordMapper.updateById(row);
        invalidateHotCache();
    }

    @Override
    @Transactional
    public AdminSearchHotKeywordVO updateBoostScore(Long id, Integer boostScore) {
        SearchHotKeyword row = requireRow(id);
        int safeBoost = boostScore == null ? 0 : Math.max(0, Math.min(boostScore, 999999));
        row.setBoostScore(safeBoost);
        hotKeywordMapper.updateById(row);
        invalidateHotCache();
        SearchHotKeyword updated = hotKeywordMapper.selectById(id);
        AdminSearchHotKeywordVO vo = toVo(updated);
        vo.setRank(computeRank(updated));
        return vo;
    }

    private int computeRank(SearchHotKeyword target) {
        List<SearchHotKeyword> enabled = hotKeywordMapper.selectList(
                new LambdaQueryWrapper<SearchHotKeyword>()
                        .eq(SearchHotKeyword::getDeleted, 0)
                        .eq(SearchHotKeyword::getStatus, 1));
        enabled.sort(Comparator
                .comparingInt(this::effectiveScore).reversed()
                .thenComparing(SearchHotKeyword::getSearchCount, Comparator.reverseOrder())
                .thenComparing(SearchHotKeyword::getId));
        for (int i = 0; i < enabled.size(); i++) {
            if (enabled.get(i).getId().equals(target.getId())) {
                return i + 1;
            }
        }
        return 0;
    }

    private SearchHotKeyword requireRow(Long id) {
        SearchHotKeyword row = hotKeywordMapper.selectById(id);
        if (row == null || (row.getDeleted() != null && row.getDeleted() != 0)) {
            throw new BusinessException(404, "搜索热词不存在");
        }
        return row;
    }

    private int effectiveScore(SearchHotKeyword row) {
        int count = row.getSearchCount() == null ? 0 : row.getSearchCount();
        int boost = row.getBoostScore() == null ? 0 : row.getBoostScore();
        return count + boost;
    }

    private AdminSearchHotKeywordVO toVo(SearchHotKeyword row) {
        AdminSearchHotKeywordVO vo = new AdminSearchHotKeywordVO();
        vo.setId(row.getId());
        vo.setKeyword(row.getKeyword());
        vo.setSearchCount(row.getSearchCount());
        vo.setBoostScore(row.getBoostScore() == null ? 0 : row.getBoostScore());
        vo.setStatus(row.getStatus());
        if (row.getUpdateTime() != null) {
            vo.setUpdateTime(row.getUpdateTime().format(DT_FMT));
        }
        return vo;
    }

    private int normalizeStatus(Integer status) {
        return status != null && status == 0 ? 0 : 1;
    }

    private void invalidateHotCache() {
        searchCacheService.invalidatePrefix("hot:");
    }
}
