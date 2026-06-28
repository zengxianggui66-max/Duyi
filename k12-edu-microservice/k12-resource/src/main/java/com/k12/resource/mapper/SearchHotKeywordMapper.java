package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.resource.entity.SearchHotKeyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 热搜关键词Mapper
 */
@Mapper
public interface SearchHotKeywordMapper extends BaseMapper<SearchHotKeyword> {
    
    /**
     * 自增搜索次数
     */
    @Update("UPDATE search_hot_keyword SET search_count = search_count + 1 WHERE id = #{id}")
    void incrementSearchCount(@Param("id") Long id);
}
