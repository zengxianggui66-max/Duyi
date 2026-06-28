package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.EduResourceBrand;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface EduResourceBrandMapper extends BaseMapper<EduResourceBrand> {

    @Select("<script>" +
            "SELECT * FROM edu_resource_brand WHERE status = 1 " +
            "ORDER BY sort ASC, id ASC" +
            "</script>")
    List<EduResourceBrand> listActive();

    /** 仅查 id/code，兼容未执行 logo_url 补丁的旧库 */
    @Select("<script>"
            + "SELECT id, code FROM edu_resource_brand WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<EduResourceBrand> selectCodeByIds(@Param("ids") Collection<Long> ids);
}
