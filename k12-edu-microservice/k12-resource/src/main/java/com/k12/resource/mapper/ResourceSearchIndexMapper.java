package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.resource.entity.ResourceSearchIndex;
import com.k12.resource.search.SearchFacetRow;
import com.k12.resource.search.SearchIndexSqlParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceSearchIndexMapper extends BaseMapper<ResourceSearchIndex> {

    long countSearch(@Param("q") SearchIndexSqlParam param);

    List<ResourceSearchIndex> searchPage(@Param("q") SearchIndexSqlParam param);

    List<SearchFacetRow> facetStages(@Param("q") SearchIndexSqlParam param);

    List<SearchFacetRow> facetChannels(@Param("q") SearchIndexSqlParam param);

    List<SearchFacetRow> facetTypes(@Param("q") SearchIndexSqlParam param);
}
