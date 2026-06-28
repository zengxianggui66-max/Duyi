package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.UnitDoc;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 单元文档Mapper接口
 */
@Mapper
public interface UnitDocMapper extends BaseMapper<UnitDoc> {
    
    /**
     * 根据单元名称模糊查询
     */
    @Select("SELECT * FROM oss_unit_doc WHERE unit_name LIKE CONCAT('%', #{unitName}, '%') " +
            "AND is_deleted = 0 ORDER BY upload_time DESC")
    List<UnitDoc> findByUnitNameLike(@Param("unitName") String unitName);
    
    /**
     * 根据文件名模糊查询
     */
    @Select("SELECT * FROM oss_unit_doc WHERE original_filename LIKE CONCAT('%', #{filename}, '%') " +
            "AND is_deleted = 0 ORDER BY upload_time DESC")
    List<UnitDoc> findByFilenameLike(@Param("filename") String filename);
    
    /**
     * 根据文件类型查询
     */
    @Select("<script>" +
            "SELECT * FROM oss_unit_doc " +
            "WHERE is_deleted = 0 " +
            "AND (${fileTypeCondition}) " +
            "ORDER BY upload_time DESC" +
            "</script>")
    List<UnitDoc> findByFileType(@Param("fileTypeCondition") String fileTypeCondition);
    
    /**
     * 根据文件大小范围查询
     */
    @Select("SELECT * FROM oss_unit_doc WHERE file_size_kb BETWEEN #{minSize} AND #{maxSize} " +
            "AND is_deleted = 0 ORDER BY file_size_kb ASC")
    List<UnitDoc> findBySizeRange(@Param("minSize") Integer minSize, 
                                  @Param("maxSize") Integer maxSize);
    
    /**
     * 获取统计信息
     */
    @Select("SELECT " +
            "COUNT(*) as total_count, " +
            "SUM(file_size_kb) as total_size_kb, " +
            "AVG(file_size_kb) as avg_size_kb, " +
            "MAX(upload_time) as latest_upload " +
            "FROM oss_unit_doc WHERE is_deleted = 0")
    Map<String, Object> getStatistics();
    
    /**
     * 批量更新OSS URL
     */
    @Update("<script>" +
            "<foreach collection='list' item='item' separator=';'>" +
            "UPDATE oss_unit_doc SET oss_url = #{item.ossUrl} " +
            "WHERE id = #{item.id} AND is_deleted = 0" +
            "</foreach>" +
            "</script>")
    int batchUpdateOssUrl(@Param("list") List<UnitDoc> list);
    
    /**
     * 根据单元名称获取所有文档
     */
    @Select("SELECT * FROM oss_unit_doc WHERE unit_name = #{unitName} " +
            "AND is_deleted = 0 ORDER BY upload_time DESC")
    List<UnitDoc> findByUnitName(@Param("unitName") String unitName);
}
