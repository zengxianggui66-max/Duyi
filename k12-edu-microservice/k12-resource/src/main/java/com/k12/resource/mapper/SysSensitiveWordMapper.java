package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.SysSensitiveWord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Phase 8：敏感词库 Mapper
 */
@Mapper
public interface SysSensitiveWordMapper extends BaseMapper<SysSensitiveWord> {

    /**
     * 分页查询敏感词（支持按 word 模糊搜索 + category + level + status 筛选）
     */
    @Select("<script>" +
            "SELECT * FROM sys_sensitive_word WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'> AND word LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "<if test='category != null'> AND category = #{category} </if>" +
            "<if test='level != null'> AND level = #{level} </if>" +
            "<if test='status != null'> AND status = #{status} </if>" +
            "ORDER BY update_time DESC" +
            "</script>")
    Page<SysSensitiveWord> findPage(Page<SysSensitiveWord> page,
                                     @Param("keyword") String keyword,
                                     @Param("category") Integer category,
                                     @Param("level") Integer level,
                                     @Param("status") Integer status);

    /**
     * 获取所有启用的敏感词列表
     */
    @Select("SELECT word FROM sys_sensitive_word WHERE status = 1 AND level = #{level}")
    List<String> findEnabledWordsByLevel(@Param("level") Integer level);

    /**
     * 获取所有启用的敏感词（用于检测）
     */
    @Select("SELECT word FROM sys_sensitive_word WHERE status = 1")
    List<String> findAllEnabledWords();

    /**
     * 按 id 列表批量更新 status
     */
    @Select("<script>" +
            "UPDATE sys_sensitive_word SET status = #{status}, update_time = NOW() " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "  #{id}" +
            "</foreach>" +
            "</script>")
    int batchUpdateStatus(@Param("ids") List<Long> ids, @Param("status") Integer status);

    /**
     * 按分类统计
     */
    @Select("SELECT category, COUNT(*) AS cnt FROM sys_sensitive_word WHERE status = 1 GROUP BY category")
    List<java.util.Map<String, Object>> countByCategory();
}
