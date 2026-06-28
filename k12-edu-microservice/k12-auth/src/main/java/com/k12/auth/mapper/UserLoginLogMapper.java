package com.k12.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.UserLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLog> {

    @Select("SELECT create_time FROM user_login_log WHERE user_id = #{userId} AND success = 1 " +
            "ORDER BY create_time DESC LIMIT 1")
    LocalDateTime findLastSuccessTime(@Param("userId") Long userId);
}
