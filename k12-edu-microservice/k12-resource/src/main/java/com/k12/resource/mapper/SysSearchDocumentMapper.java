package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.resource.entity.SysSearchDocument;
import com.k12.resource.search.SearchDocumentSqlParam;
import com.k12.resource.search.SearchFacetRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysSearchDocumentMapper extends BaseMapper<SysSearchDocument> {

    long countSearch(@Param("q") SearchDocumentSqlParam q);

    List<SysSearchDocument> searchPage(@Param("q") SearchDocumentSqlParam q);

    List<SearchFacetRow> facetStages(@Param("q") SearchDocumentSqlParam q);

    List<SearchFacetRow> facetChannels(@Param("q") SearchDocumentSqlParam q);

    List<SearchFacetRow> facetTypes(@Param("q") SearchDocumentSqlParam q);

    List<SysSearchDocument> suggestByKind(@Param("q") SearchDocumentSqlParam q, @Param("docTypes") List<String> docTypes);

    List<SysSearchDocument> searchBroadPage(@Param("q") SearchDocumentSqlParam q);

    long countBroadSearch(@Param("q") SearchDocumentSqlParam q);

    List<SysSearchDocument> searchExactTitle(@Param("q") SearchDocumentSqlParam q);

    List<SysSearchDocument> findByDocIds(@Param("docIds") List<String> docIds, @Param("hideVip") boolean hideVip);
}
