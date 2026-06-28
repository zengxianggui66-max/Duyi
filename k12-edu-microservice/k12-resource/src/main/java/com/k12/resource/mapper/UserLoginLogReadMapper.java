package com.k12.resource.mapper;

import com.k12.common.dto.AdminUserLoginLogVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserLoginLogReadMapper {

    @Select("""
            SELECT COUNT(1) FROM user_login_log WHERE user_id = #{userId}
            """)
    long countByUser(@Param("userId") Long userId);

    @Select("""
            SELECT id, login_type AS loginType, success, fail_reason AS failReason,
                   ip, create_time AS createTime
            FROM user_login_log
            WHERE user_id = #{userId}
            ORDER BY create_time DESC
            LIMIT #{offset}, #{size}
            """)
    List<AdminUserLoginLogVO> selectPageByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size);
}
