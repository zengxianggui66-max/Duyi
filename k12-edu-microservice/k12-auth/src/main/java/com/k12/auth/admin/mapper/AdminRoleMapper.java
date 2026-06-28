package com.k12.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.auth.admin.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminRoleMapper extends BaseMapper<SysRole> {
}
