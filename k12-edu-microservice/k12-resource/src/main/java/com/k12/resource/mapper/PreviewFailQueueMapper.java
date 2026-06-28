package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.k12.common.entity.PreviewFailQueue;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * Phase 8：预览失败队列 Mapper
 */
@Mapper
public interface PreviewFailQueueMapper extends BaseMapper<PreviewFailQueue> {

    /**
     * 分页查询预览失败队列（支持按 status + sourceType 筛选）
     */
    @Select("<script>" +
            "SELECT * FROM preview_fail_queue WHERE 1=1 " +
            "<if test='status != null'> AND status = #{status} </if>" +
            "<if test='sourceType != null and sourceType != \"\"'> AND source_type = #{sourceType} </if>" +
            "<if test='keyword != null and keyword != \"\"'> AND title LIKE CONCAT('%', #{keyword}, '%') </if>" +
            "ORDER BY last_fail_time DESC" +
            "</script>")
    Page<PreviewFailQueue> findPage(Page<PreviewFailQueue> page,
                                     @Param("status") Integer status,
                                     @Param("sourceType") String sourceType,
                                     @Param("keyword") String keyword);

    /**
     * 标记已处理
     */
    @Update("UPDATE preview_fail_queue SET status = 1, handler_id = #{handlerId}, handler_name = #{handlerName}, " +
            "handler_note = #{note}, handler_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int markHandled(@Param("id") Long id,
                    @Param("handlerId") Long handlerId,
                    @Param("handlerName") String handlerName,
                    @Param("note") String note);

    /**
     * 标记已忽略
     */
    @Update("UPDATE preview_fail_queue SET status = 2, handler_id = #{handlerId}, handler_name = #{handlerName}, " +
            "handler_note = #{note}, handler_time = NOW(), update_time = NOW() WHERE id = #{id}")
    int markIgnored(@Param("id") Long id,
                    @Param("handlerId") Long handlerId,
                    @Param("handlerName") String handlerName,
                    @Param("note") String note);

    /**
     * 统计待处理数量
     */
    @Select("SELECT COUNT(*) FROM preview_fail_queue WHERE status = 0")
    int countPending();

    /**
     * 按状态分组统计
     */
    @Select("SELECT status, COUNT(*) AS cnt FROM preview_fail_queue GROUP BY status")
    java.util.List<java.util.Map<String, Object>> countByStatus();
}
