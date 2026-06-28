package com.k12.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.FileFormatConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 文件格式配置Mapper
 */
@Mapper
public interface FileFormatConfigMapper extends BaseMapper<FileFormatConfig> {
    
    /**
     * 根据扩展名查询格式配置
     */
    @Select("SELECT * FROM file_format_config WHERE extension = #{extension} AND status = 1 LIMIT 1")
    FileFormatConfig findByExtension(@Param("extension") String extension);
}
