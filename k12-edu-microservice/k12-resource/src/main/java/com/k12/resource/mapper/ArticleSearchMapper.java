package com.k12.resource.mapper;

import com.k12.common.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ArticleSearchMapper {

    @Select("SELECT id, title, summary, content, cover_url AS coverUrl, category, category_name AS categoryName, "
            + "tags, sub_category AS subCategory, publish_time AS publishTime, view_count AS viewCount, "
            + "status, deleted, source_name AS sourceName "
            + "FROM article WHERE status = 1 AND deleted = 0")
    List<Article> selectPublishedAll();

    @Select("SELECT id, title, summary, content, cover_url AS coverUrl, category, category_name AS categoryName, "
            + "tags, sub_category AS subCategory, publish_time AS publishTime, view_count AS viewCount, "
            + "status, deleted, source_name AS sourceName "
            + "FROM article WHERE id = #{id}")
    Article selectById(Long id);
}
