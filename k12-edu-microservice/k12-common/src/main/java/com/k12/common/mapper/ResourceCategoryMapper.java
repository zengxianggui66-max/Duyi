package com.k12.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.ResourceCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 资源分类 Mapper（表 resource_category）
 *
 * @deprecated since Phase 3K — 无 Service 注入。见 {@code docs/Phase3K-表分层冻结清单.md}
 */
@Deprecated(since = "3K", forRemoval = true)
@Mapper
public interface ResourceCategoryMapper extends BaseMapper<ResourceCategory> {
    
    /**
     * 查询所有启用的分类
     */
    @Select("SELECT * FROM resource_category WHERE status = 1 AND deleted = 0 ORDER BY sort_order")
    List<ResourceCategory> findAllEnabled();
    
    /**
     * 根据父ID查询子分类
     */
    @Select("SELECT * FROM resource_category WHERE parent_id = #{parentId} AND status = 1 AND deleted = 0 ORDER BY sort_order")
    List<ResourceCategory> findByParentId(@Param("parentId") Long parentId);
}
