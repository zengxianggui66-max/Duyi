package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.UserResourceAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserResourceActionMapper extends BaseMapper<UserResourceAction> {

    @Select("""
            SELECT COUNT(1) FROM user_resource_action
            WHERE user_id = #{userId} AND action_type = #{actionType}
            """)
    long countByUserAndType(@Param("userId") Long userId, @Param("actionType") String actionType);
}
