package com.k12.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.UploadRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件上传记录Mapper
 */
@Mapper
public interface UploadRecordMapper extends BaseMapper<UploadRecord> {
}
