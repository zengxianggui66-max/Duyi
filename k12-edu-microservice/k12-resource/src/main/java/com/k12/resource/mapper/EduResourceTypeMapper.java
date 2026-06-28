package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduResourceType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源类型 Mapper
 */
@Mapper
public interface EduResourceTypeMapper extends BaseMapper<EduResourceType> {

    @Select("<script>" +
            "SELECT id, parent_id, code, name, icon, group_code, group_name, allow_preview, sort, status " +
            "FROM xinketang.edu_resource_type " +
            "WHERE parent_id &gt; 0 " +
            "<if test='!includeDisabled'> AND status = 1 </if>" +
            "<if test='groupCode != null and groupCode != \"\"'> AND group_code = #{groupCode} </if>" +
            "ORDER BY sort ASC, id ASC" +
            "</script>")
    List<EduResourceType> selectLeafTypes(
            @Param("groupCode") String groupCode,
            @Param("includeDisabled") boolean includeDisabled);
}
