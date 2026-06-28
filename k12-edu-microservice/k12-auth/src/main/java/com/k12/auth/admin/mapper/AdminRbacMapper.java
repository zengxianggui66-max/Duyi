package com.k12.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.k12.auth.admin.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AdminRbacMapper extends BaseMapper<SysMenu> {

    @Select("""
            SELECT p.code
            FROM sys_permission p
            INNER JOIN sys_role_permission rp ON rp.permission_id = p.id
            INNER JOIN sys_user_role ur ON ur.role_id = rp.role_id
            WHERE ur.user_id = #{userId}
              AND p.status = 1
            GROUP BY p.code, p.sort
            ORDER BY p.sort
            """)
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT r.code
            FROM sys_role r
            INNER JOIN sys_user_role ur ON ur.role_id = r.id
            WHERE ur.user_id = #{userId}
              AND r.status = 1
            """)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT id, parent_id, title, path, name, icon, component, permission_code, sort, hidden, status, create_time
            FROM sys_menu
            WHERE status = 1 AND hidden = 0
            ORDER BY sort ASC, id ASC
            """)
    List<SysMenu> selectActiveMenus();
}
