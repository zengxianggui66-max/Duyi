package com.k12.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.common.entity.HomeFuncChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HomeFuncChannelMapper extends BaseMapper<HomeFuncChannel> {

    @Select("SELECT * FROM xinketang.home_func_channel " +
            "WHERE status = 1 ORDER BY sort ASC, id ASC")
    List<HomeFuncChannel> findActiveOrdered();
}
